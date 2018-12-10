package com.bridou_n.beaconscanner.features.beaconList

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.BluetoothManager
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.BeaconConsumer
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

class BeaconListActivity : AppCompatActivity(), BeaconListContract.View, BeaconConsumer, EasyPermissions.PermissionCallbacks {

    companion object {
        val coarseLocationPermission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        val RC_COARSE_LOCATION = 1
        val RC_SETTINGS_SCREEN = 2
    }

    enum class BluetoothState(val bgColor: Int, val text: Int) {
        STATE_OFF(R.color.bluetoothDisabled, R.string.bluetooth_disabled),
        STATE_TURNING_OFF(R.color.bluetoothTurningOff, R.string.turning_bluetooth_off),
        STATE_ON(R.color.bluetoothTurningOn, R.string.bluetooth_enabled),
        STATE_TURNING_ON(R.color.bluetoothTurningOn, R.string.turning_bluetooth_on)
    }

    @Inject lateinit var bluetoothState: BluetoothManager
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var realm: Realm
    @Inject lateinit var loggingService: LoggingService
    @Inject lateinit var prefs: PreferencesHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    private var dialog: MaterialDialog? = null
    private var menu: Menu? = null
    private lateinit var presenter: BeaconListContract.Presenter

    private val rvAdapter by lazy {
        BeaconsRecyclerViewAdapter(this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
            override fun onOpenControls(beacon: BeaconSaved) {
                ControlsBottomSheetDialog.newInstance(beacon).apply {
                    show(supportFragmentManager, this.tag)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        component().inject(this)

        toolbar.inflateMenu(R.menu.main_menu)
        setSupportActionBar(toolbar)

        beacons_rv.setHasFixedSize(true)
        beacons_rv.layoutManager = LinearLayoutManager(this)
        beacons_rv.adapter = rvAdapter

        presenter = BeaconListPresenter(this, rxBus, prefs, realm, loggingService, bluetoothState, tracker)

        scan_fab.setOnClickListener {
            presenter.toggleScan()
        }
    }

    override fun showTutorial() : Boolean {
        val btIcon = toolbar.findViewById<View?>(R.id.action_bluetooth)
        val clearIcon = toolbar.findViewById<View?>(R.id.action_clear)

        if (menu == null || btIcon == null || clearIcon == null) { // If the menu is not inflated yet
            return false
        }

        TapTargetSequence(this)
                .targets(
                        // TODO: double check this
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content))
                                .cancelable(false)
                                .dimColor(R.color.colorOnBackground)
                                .drawShadow(true),
                        TapTarget.forView(scan_fab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content))
                                .tintTarget(false)
                                .cancelable(false)
                                .dimColor(R.color.colorOnBackground)
                                .drawShadow(true)
                        ,
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content))
                                .cancelable(false)
                                .dimColor(R.color.colorOnBackground)
                                .drawShadow(true)
                )
                .start()
        return true
    }

    override fun onResume() {
        super.onResume()
        presenter.setBeaconManager(component().providesBeaconManager())
        presenter.start()
    }

    override fun keepScreenOn(status: Boolean) {
        if (status) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun submitData(list: List<BeaconSaved>) {
        rvAdapter.submitList(list)
    }

    override fun showEmptyView(show: Boolean) {
        empty_view.visibility = if (show) View.VISIBLE else View.GONE
        beacons_rv.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun updateBluetoothState(state: BluetoothState, isEnabled: Boolean) {
        bluetooth_state.visibility = View.VISIBLE
        bluetooth_state.setBackgroundColor(ContextCompat.getColor(this, state.bgColor))
        bluetooth_state.text = getString(state.text)

        val icon = AppCompatResources.getDrawable(this, if (isEnabled) R.drawable.ic_round_bluetooth_24px else R.drawable.ic_round_bluetooth_disabled_24px)
                ?.mutate()
        icon?.setColorFilter(ContextCompat.getColor(this, R.color.toolbarTextColor), PorterDuff.Mode.SRC_IN)

        menu?.getItem(1)?.icon = icon

        // If the bluetooth is ON, we don't warn the user
        if (state == BluetoothState.STATE_ON) {
            bluetooth_state.visibility = View.GONE
        }
    }

    /* Permissions methods */
    override fun hasCoarseLocationPermission() = EasyPermissions.hasPermissions(this, *coarseLocationPermission)

    override fun hasSomePermissionPermanentlyDenied(perms: List<String>) = EasyPermissions.somePermissionPermanentlyDenied(this, perms)

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_COARSE_LOCATION) {
            presenter.onLocationPermissionGranted()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, permList: List<String>) {
        if (requestCode == RC_COARSE_LOCATION) {
            presenter.onLocationPermissionDenied(requestCode, permList)
        }
    }

    override fun showEnablePermissionSnackbar() {
        Snackbar.make(root_view, getString(R.string.enable_permission_from_settings), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.enable)) { _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    startActivityForResult(intent, RC_SETTINGS_SCREEN)
                }.show()
    }

    override fun askForCoarseLocationPermission() = ActivityCompat.requestPermissions(this, coarseLocationPermission, RC_COARSE_LOCATION)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /* ==== end of permission methods ==== */

    override fun showBluetoothNotEnabledError() {
        Snackbar.make(root_view, getString(R.string.enable_bluetooth_to_start_scanning), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.enable)) { _ ->
                    presenter.onBluetoothToggle()
                }
                .show()
    }

    override fun showGenericError(msg: String) {
        Snackbar.make(root_view, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoggingError() = Snackbar.make(root_view, getString(R.string.logging_error_please_check), Snackbar.LENGTH_LONG).show()

    override fun showScanningState(state: Boolean) {
        toolbar.title = getString(if (state) R.string.scanning_for_beacons else R.string.app_name)
        progress_1.visibility = if (state) View.VISIBLE else View.GONE
        progress_2.visibility = if (state) View.VISIBLE else View.GONE

        scan_fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, if (state) R.color.colorPauseFab else R.color.colorSecondary))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val anim = AnimatedVectorDrawableCompat.create(this, if (state) R.drawable.play_to_pause else R.drawable.pause_to_play) as AnimatedVectorDrawableCompat

            scan_fab.setImageDrawable(anim)
            anim.start()
        } else {
            scan_fab.setImageDrawable(AppCompatResources.getDrawable(this, if (state) R.drawable.pause_icon else R.drawable.play_icon))
        }
    }

    override fun onBeaconServiceConnect() = presenter.onBeaconServiceConnect()

    override fun redirectToStorePage() {
        val appPackageName = packageName

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        this.menu = menu
        return true
    }

    override fun showClearDialog() {
        dialog = MaterialDialog(this)
                .title(R.string.delete_all)
                .message(R.string.are_you_sure_delete_all)
                .positiveButton(android.R.string.ok, click = {
                    presenter.onClearAccepted()
                })
                .negativeButton(android.R.string.cancel)
        dialog?.show()
    }

    override fun startSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_bluetooth -> {
                presenter.onBluetoothToggle()
            }
            R.id.action_clear -> {
                presenter.onClearClicked()
            }
            R.id.action_settings -> {
                presenter.onSettingsClicked()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPause() {
        presenter.stop()
        super.onPause()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        presenter.clear()
        super.onDestroy()
    }
}
