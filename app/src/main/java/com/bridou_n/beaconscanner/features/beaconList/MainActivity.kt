package com.bridou_n.beaconscanner.features.beaconList

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.events.Events
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.BluetoothManager
import com.bridou_n.beaconscanner.utils.DividerItemDecoration
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), BeaconConsumer, EasyPermissions.PermissionCallbacks {

    companion object {
        protected val TAG = "MAIN_ACTIVITY"
        private val perms = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        private val RC_COARSE_LOCATION = 1
        private val RC_SETTINGS_SCREEN = 2
    }

    private var bluetoothStateDisposable: Disposable? = null
    private var rangeDisposable: Disposable? = null
    private var dialog: MaterialDialog? = null
    private var hasStartedTutorial = false
    private lateinit var beaconResults: RealmResults<BeaconSaved>

    @Inject lateinit var bluetoothState: BluetoothManager
    @Inject lateinit var beaconManager: BeaconManager
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var realm: Realm
    @Inject lateinit var prefs: PreferencesHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.activity_main) lateinit var rootView: CoordinatorLayout
    @BindView(R.id.bluetooth_state) lateinit var bluetoothStateTv: TextView

    @BindView(R.id.empty_view) lateinit var emptyView: RelativeLayout
    @BindView(R.id.beacons_rv) lateinit var beaconsRv: RecyclerView

    @BindView(R.id.scan_fab) lateinit var scanFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        component().inject(this)

        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.main_menu)
        progress.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor), PorterDuff.Mode.MULTIPLY)

        beaconResults = realm.where(BeaconSaved::class.java).findAllSortedAsync(arrayOf("lastMinuteSeen", "distance"), arrayOf(Sort.DESCENDING, Sort.ASCENDING))

        beaconsRv.setHasFixedSize(true)
        beaconsRv.layoutManager = LinearLayoutManager(this)
        beaconsRv.addItemDecoration(DividerItemDecoration(this, null))
        beaconsRv.adapter = BeaconsRecyclerViewAdapter(beaconResults!!)

        // Setup an observable on the bluetooth changes
        bluetoothStateDisposable = bluetoothState.asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { e ->
                    if (e is Events.BluetoothState) {
                        bluetoothStateChanged(e.state)
                    }
                }
    }

    fun showTutorial() {
        TapTargetView.showFor(this,
                TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        bluetoothState.enable()
                        TapTargetView.showFor(this@MainActivity,
                                TapTarget.forView(scanFab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content)).tintTarget(false).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                object : TapTargetView.Listener() {
                                    override fun onTargetClick(view: TapTargetView) {
                                        super.onTargetClick(view)
                                        startScan()  // We start scanning for beacons
                                        TapTargetView.showFor(this@MainActivity,
                                                TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                                object : TapTargetView.Listener() {
                                                    override fun onTargetClick(view: TapTargetView) {
                                                        prefs.setHasSeenTutorial(true)
                                                        hasStartedTutorial = false
                                                        super.onTargetClick(view)
                                                    }
                                                })
                                    }
                                })
                    }
                })
    }

    override fun onResume() {
        beaconResults.addChangeListener { results ->
            if (results.size == 0 && emptyView.visibility != View.VISIBLE) {
                beaconsRv.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else if (results.size > 0 && beaconsRv.visibility != View.VISIBLE) {
                beaconsRv.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        }

        if (!prefs.hasSeenTutorial() && !hasStartedTutorial) {
            hasStartedTutorial = true
            showTutorial()
        }

        // Start scanning if the scan on open is activated or if we were previously scanning
        if (prefs.isScanOnOpen && !isScanning || prefs.wasScanning()) {
            if (bluetoothState.isEnabled) {
                startScan()
            }
        }
        super.onResume()
    }

    private fun updateUiWithBeaconsArround(beacons: Collection<Beacon>) {
        realm.executeTransactionAsync { tRealm ->
            Observable.fromIterable(beacons)
                    .subscribe { b ->
                        val beacon = BeaconSaved()

                        // Common field to every beacon
                        beacon.hashcode = b.hashCode()
                        beacon.lastSeen = Date()
                        beacon.lastMinuteSeen = Date().time / 1000 / 60
                        beacon.beaconAddress = b.bluetoothAddress
                        beacon.rssi = b.rssi
                        beacon.manufacturer = b.manufacturer
                        beacon.txPower = b.txPower
                        beacon.distance = b.distance
                        if (b.serviceUuid == 0xfeaa) { // This is an Eddystone beacon
                            // Do we have telemetry data?
                            if (b.extraDataFields.size > 0) {
                                beacon.isHasTelemetryData = true
                                beacon.telemetryVersion = b.extraDataFields[0]
                                beacon.batteryMilliVolts = b.extraDataFields[1]
                                beacon.setTemperature(b.extraDataFields[2].toFloat())
                                beacon.pduCount = b.extraDataFields[3]
                                beacon.uptime = b.extraDataFields[4]
                            } else {
                                beacon.isHasTelemetryData = false
                            }

                            when (b.beaconTypeCode) {
                                0x00 -> {
                                    beacon.beaconType = BeaconSaved.TYPE_EDDYSTONE_UID
                                    // This is a Eddystone-UID frame
                                    beacon.namespaceId = b.id1.toString()
                                    beacon.instanceId = b.id2.toString()
                                }
                                0x10 -> {
                                    beacon.beaconType = BeaconSaved.TYPE_EDDYSTONE_URL
                                    // This is a Eddystone-URL frame
                                    beacon.url = UrlBeaconUrlCompressor.uncompress(b.id1.toByteArray())
                                }
                            }
                        } else { // This is an iBeacon or ALTBeacon
                            beacon.beaconType = if (b.beaconTypeCode == 0xbeac) BeaconSaved.TYPE_ALTBEACON else BeaconSaved.TYPE_IBEACON // 0x4c000215 is iBeacon
                            beacon.uuid = b.id1.toString()
                            beacon.major = b.id2.toString()
                            beacon.minor = b.id3.toString()
                        }

                        val infos = Bundle()

                        infos.putInt("manufacturer", beacon.manufacturer)
                        infos.putInt("type", beacon.beaconType)
                        infos.putDouble("distance", beacon.distance)

                        tracker.logEvent("adding_or_updating_beacon", infos)
                        tRealm.copyToRealmOrUpdate(beacon)
                    }
        }
    }

    private fun bluetoothStateChanged(state: Int) {
        bluetoothStateTv.visibility = View.VISIBLE
        when (state) {
            BluetoothAdapter.STATE_OFF -> {
                bluetoothStateTv.setTextColor(ContextCompat.getColor(this, R.color.bluetoothDisabledLight))
                bluetoothStateTv.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothDisabled))
                bluetoothStateTv.text = getString(R.string.bluetooth_disabled)
                stopScan()
                invalidateOptionsMenu()
            }
            BluetoothAdapter.STATE_TURNING_OFF -> {
                bluetoothStateTv.setTextColor(ContextCompat.getColor(this, R.color.bluetoothTurningOffLight))
                bluetoothStateTv.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothTurningOff))
                bluetoothStateTv.text = getString(R.string.turning_bluetooth_off)
            }
            BluetoothAdapter.STATE_ON -> {
                bluetoothStateTv.visibility = View.GONE // If the bluetooth is ON, we don't warn the user
                bluetoothStateTv.text = getString(R.string.bluetooth_enabled)
                invalidateOptionsMenu()
            }
            BluetoothAdapter.STATE_TURNING_ON -> {
                bluetoothStateTv.setTextColor(ContextCompat.getColor(this, R.color.bluetoothTurningOnLight))
                bluetoothStateTv.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothTurningOn))
                bluetoothStateTv.text = getString(R.string.turning_bluetooth_on)
            }
        }
    }

    fun bindBeaconManager(): Boolean {
        if (EasyPermissions.hasPermissions(this, *perms)) { // Ask permission and bind the beacon manager
            if (!beaconManager.isBound(this)) {
                beaconManager.bind(this)
            }
            return true
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, perms, RC_COARSE_LOCATION)
        }
        return false
    }

    @OnClick(R.id.scan_fab)
    fun startStopScan() {
        if (!isScanning) {
            tracker.logEvent("start_scanning_clicked", null)
            if (!bluetoothState.isEnabled) {
                Snackbar.make(rootView, getString(R.string.enable_bluetooth_to_start_scanning), Snackbar.LENGTH_LONG).show()
                return
            }
            startScan()
        } else {
            tracker.logEvent("stop_scanning_clicked", null)
            stopScan()
        }
    }

    val isScanning: Boolean
        get() = rangeDisposable != null && !rangeDisposable!!.isDisposed

    fun startScan() {
        if (!isScanning && bindBeaconManager()) {
            rangeDisposable = rxBus.asFlowable() // Listen for range events
                    .observeOn(AndroidSchedulers.mainThread()) // We use this so we use the realm on the good thread & we can make UI changes
                    .subscribe { e ->
                        if (e is Events.RangeBeacon) {
                            updateUiWithBeaconsArround(e.beacons)
                        }
                    }

            toolbar.title = getString(R.string.scanning_for_beacons)
            progress.visibility = View.VISIBLE
            scanFab.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorPauseFab))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val playToPause = ContextCompat.getDrawable(this, R.drawable.play_to_pause_animation) as AnimatedVectorDrawable

                scanFab.setImageDrawable(playToPause)
                playToPause.start()
            } else {
                scanFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause_icon))
            }
        }
    }

    fun stopScan() {
        if (isScanning) {
            rangeDisposable!!.dispose() // Stop listening for range events

            toolbar.title = getString(R.string.app_name)
            progress.visibility = View.GONE
            scanFab.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorAccent))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val pauseToPlay = ContextCompat.getDrawable(this, R.drawable.pause_to_play_animation) as AnimatedVectorDrawable

                scanFab.setImageDrawable(pauseToPlay)
                pauseToPlay.start()
            } else {
                scanFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play_icon))
            }
        }
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier { beacons, region -> rxBus.send(Events.RangeBeacon(beacons, region)) }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("com.bridou_n.beaconscanner", null, null, null))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        tracker.logEvent("permission_granted", null)
        startScan()
    }

    override fun onPermissionsDenied(requestCode: Int, permList: List<String>) {
        if (requestCode == RC_COARSE_LOCATION) {
            tracker.logEvent("permission_denied", null)
            if (EasyPermissions.somePermissionPermanentlyDenied(this, permList)) {
                tracker.logEvent("permission_denied_permanently", null)
                showPermissionSnackbar()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, perms, RC_COARSE_LOCATION)
            }
        }
    }

    fun showPermissionSnackbar() {
        val snackBar = Snackbar.make(rootView, getString(R.string.enable_permission_from_settings), Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(getString(R.string.enable)) { _ ->
            snackBar.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivityForResult(intent, RC_SETTINGS_SCREEN)
        }
        snackBar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (!bluetoothState.isEnabled) {
            menu.getItem(1).setIcon(R.drawable.ic_bluetooth_disabled_white_24dp)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_bluetooth -> {
                bluetoothState.toggle()
                tracker.logEvent("action_bluetooth", null)
            }
            R.id.action_clear -> {
                tracker.logEvent("action_clear", null)
                dialog = MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title(R.string.delete_all)
                        .content(R.string.are_you_sure_delete_all)
                        .autoDismiss(true)
                        .onPositive { _, _ ->
                            tracker.logEvent("action_clear_accepted", null)
                            realm.executeTransactionAsync { tRealm -> tRealm.where(BeaconSaved::class.java).findAll().deleteAllFromRealm() }
                        }
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .build()
                dialog?.show()
            }
            R.id.action_settings -> {
                tracker.logEvent("action_settings", null)
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPause() {
        prefs.setScanningState(isScanning)
        stopScan()
        super.onPause()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        if (beaconManager.isBound(this)) {
            // Only do this in onDestroy() not onPause()
            // Can't be bind() & unbind() several times
            beaconManager.unbind(this)
        }
        if (bluetoothStateDisposable != null && !bluetoothStateDisposable!!.isDisposed) {
            bluetoothStateDisposable!!.dispose()
        }
        realm.close()
        super.onDestroy()
    }
}
