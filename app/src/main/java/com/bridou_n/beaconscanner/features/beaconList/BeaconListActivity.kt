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
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.BluetoothManager
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rating_bottom_sheet.*
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
    @Inject lateinit var ratingHelper: RatingHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    private var dialog: MaterialDialog? = null
    private var menu: Menu? = null
    private var bsBehavior: BottomSheetBehavior<NestedScrollView>? = null
    private lateinit var presenter: BeaconListContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeNoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        component().inject(this)

        toolbar.inflateMenu(R.menu.main_menu)
        setSupportActionBar(toolbar)

        beacons_rv.setHasFixedSize(true)
        beacons_rv.layoutManager = LinearLayoutManager(this)
        val rvAnimator = beacons_rv.itemAnimator
        if (rvAnimator is SimpleItemAnimator) {
            rvAnimator.supportsChangeAnimations = false
        }

        bsBehavior = BottomSheetBehavior.from(bottom_sheet)

        // Hide the bottomSheet uppon creation
        bsBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        presenter = BeaconListPresenter(this, rxBus, prefs, realm, loggingService, bluetoothState, ratingHelper, tracker)

        scan_fab.setOnClickListener {
            presenter.toggleScan()
        }
        listOf(positive_btn_step_1, negative_btn_step_1, positive_btn_step_2, negative_btn_step_2).forEach {
            it.setOnClickListener {
                onRatingInteraction(it)
            }
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
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content))
                                .cancelable(false)
                                .dimColor(R.color.primaryText)
                                .drawShadow(true),
                        TapTarget.forView(scan_fab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content))
                                .tintTarget(false)
                                .cancelable(false)
                                .dimColor(R.color.primaryText)
                                .drawShadow(true)
                        ,
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content))
                                .cancelable(false)
                                .dimColor(R.color.primaryText)
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

    override fun setAdapter(beaconResults: RealmResults<BeaconSaved>) {
        beacons_rv.adapter = BeaconsRecyclerViewAdapter(beaconResults, this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
            override fun onOpenControls(beacon: BeaconSaved) {
                val bsDialog = ControlsBottomSheetDialog.newInstance(beacon)
                bsDialog.show(supportFragmentManager, bsDialog.tag)
            }
        })
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

        scan_fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, if (state) R.color.colorPauseFab else R.color.colorAccent))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val anim = AnimatedVectorDrawableCompat.create(this, if (state) R.drawable.play_to_pause else R.drawable.pause_to_play) as AnimatedVectorDrawableCompat

            scan_fab.setImageDrawable(anim)
            anim.start()
        } else {
            scan_fab.setImageDrawable(AppCompatResources.getDrawable(this, if (state) R.drawable.pause_icon else R.drawable.play_icon))
        }
    }

    override fun onBeaconServiceConnect() = presenter.onBeaconServiceConnect()

    override fun showRating(step: Int, show: Boolean) {
        if (!show) {
            bsBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }

        when (step) {
            RatingHelper.STEP_ONE -> {
                bsBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                rating_step_1.visibility = View.VISIBLE
                rating_step_2.visibility = View.GONE
            }
            RatingHelper.STEP_TWO -> {
                bsBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                rating_step_1.visibility = View.GONE
                rating_step_2.visibility = View.VISIBLE
            }
        }
    }

    override fun redirectToStorePage() {
        val appPackageName = packageName

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    fun onRatingInteraction(view: View) {
        val step = when (view.id) {
            R.id.positive_btn_step_1, R.id.negative_btn_step_1 -> RatingHelper.STEP_ONE
            R.id.positive_btn_step_2, R.id.negative_btn_step_2 -> RatingHelper.STEP_TWO
            else -> RatingHelper.STEP_ONE
        }
        val answer = when (view.id) {
            R.id.positive_btn_step_1, R.id.positive_btn_step_2 -> true
            else -> false
        }

        presenter.onRatingInteraction(step, answer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        this.menu = menu
        return true
    }

    override fun showClearDialog() {
        dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(R.string.delete_all)
                .content(R.string.are_you_sure_delete_all)
                .autoDismiss(true)
                .onPositive { _, _ ->
                    presenter.onClearAccepted()
                }
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .build()
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
