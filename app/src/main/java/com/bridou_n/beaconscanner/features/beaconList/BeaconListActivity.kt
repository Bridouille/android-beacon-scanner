package com.bridou_n.beaconscanner.features.beaconList

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.room.EmptyResultSetException
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.Database.AppDatabase
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.models.LoggingRequest
import com.bridou_n.beaconscanner.utils.AndroidVersion
import com.bridou_n.beaconscanner.utils.BluetoothManager
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BeaconListActivity : AppCompatActivity(), BeaconConsumer {
	
	companion object {
		const val RC_COARSE_LOCATION = 1
	}
	
	enum class BluetoothState(
		@ColorRes val bgColor: Int,
		@StringRes val text: Int
	) {
		STATE_OFF(R.color.bluetoothDisabled, R.string.bluetooth_disabled),
		STATE_TURNING_OFF(R.color.bluetoothTurningOff, R.string.turning_bluetooth_off),
		STATE_ON(R.color.bluetoothTurningOn, R.string.bluetooth_enabled),
		STATE_TURNING_ON(R.color.bluetoothTurningOn, R.string.turning_bluetooth_on)
	}
	
	@Inject lateinit var bluetoothState: BluetoothManager
	@Inject lateinit var db: AppDatabase
	@Inject lateinit var loggingService: LoggingService
	@Inject lateinit var prefs: PreferencesHelper
	@Inject lateinit var tracker: FirebaseAnalytics
	
	private var dialog: MaterialDialog? = null
	private var menu: Menu? = null
	
	private var beaconManager: BeaconManager? = null
	private var bluetoothStateDisposable: Disposable? = null
	
	private var listQuery: Disposable? = null
	
	private var numberOfScansSinceLog = 0
	private var loggingRequests = CompositeDisposable()
	
	private var isScanning = false
	
	private val rvAdapter = BeaconsRecyclerViewAdapter { beacon ->
		ControlsBottomSheetDialog.newInstance(beacon.hashcode).apply {
			show(supportFragmentManager)
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.AppTheme)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		component().inject(this)
		
		toolbar.inflateMenu(R.menu.main_menu)
		setSupportActionBar(toolbar)
		
		beacons_rv.apply {
			adapter = rvAdapter
			viewTreeObserver.addOnScrollChangedListener {
				toolbar.isSelected = beacons_rv.canScrollVertically(-1)
			}
		}
		
		scan_fab.setOnClickListener {
			toggleScan()
		}
	}
	
	private fun toggleScan() {
		if (!isScanning()) {
			tracker.logEvent("start_scanning_clicked", null)
			return startScan()
		}
		tracker.logEvent("stop_scanning_clicked", null)
		stopScan()
	}
	
	private fun startScan() {
		if (!isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
			return reqPermission(Manifest.permission.ACCESS_COARSE_LOCATION, RC_COARSE_LOCATION)
		}
		
		if (!bluetoothState.isEnabled() || beaconManager == null) {
			return showBluetoothNotEnabledError()
		}
		
		if (beaconManager?.isBound(this) != true) {
			Timber.d("binding beaconManager")
			beaconManager?.bind(this)
		}
		
		if (prefs.preventSleep) {
			keepScreenOn(true)
		}
		
		showScanningState(true)
		isScanning = true
	}
	
	private fun stopScan() {
		unbindBeaconManager()
		showScanningState(false)
		keepScreenOn(false)
		isScanning = false
	}
	
	fun isScanning() = isScanning
	
	private fun unbindBeaconManager() {
		if (beaconManager?.isBound(this) == true) {
			Timber.d("Unbinding from beaconManager")
			beaconManager?.unbind(this)
		}
	}
	
	fun showTutorial(): Boolean {
		val btIcon = toolbar.findViewById<View?>(R.id.action_bluetooth)
		val clearIcon = toolbar.findViewById<View?>(R.id.action_clear)
		
		if (menu == null || btIcon == null || clearIcon == null) { // If the menu is not inflated yet
			return false
		}
		
		TapTargetSequence(this)
			.targets(
				TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content))
					.cancelable(false)
					.dimColor(R.color.colorOnSurface)
					.drawShadow(true),
				TapTarget.forView(scan_fab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content))
					.tintTarget(false)
					.cancelable(false)
					.dimColor(R.color.colorOnSurface)
					.drawShadow(true)
				,
				TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content))
					.cancelable(false)
					.dimColor(R.color.colorOnSurface)
					.drawShadow(true)
			)
			.start()
		return true
	}
	
	override fun onResume() {
		super.onResume()
		beaconManager = component().providesBeaconManager()
		
		observeBluetoothState()
		listQuery = db.beaconsDao().getBeacons(blocked = false)
			.subscribeOn(Schedulers.io())
			.map { list ->
				if (list.isEmpty()) {
					listOf(BeaconRow.EmptyState)
				} else {
					list.map { BeaconRow.Beacon(it) }
				}
			}
			.doOnSubscribe { rvAdapter.submitList(listOf(BeaconRow.Loading)) }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe { list ->
				Timber.d("list: $list")
				
				rvAdapter.submitList(list)
			}
		
		// Show the tutorial if needed
		if (!prefs.hasSeenTutorial()) {
			prefs.setHasSeenTutorial(showTutorial())
		}
		
		// Start scanning if we were previously scanning
		if (prefs.wasScanning()) {
			startScan()
		}
	}
	
	fun keepScreenOn(status: Boolean) {
		if (status) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		} else {
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}
	
	private fun observeBluetoothState() {
		// Setup an observable on the bluetooth changes
		bluetoothStateDisposable?.dispose()
		bluetoothStateDisposable = bluetoothState.asFlowable()
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe { newState ->
				updateBluetoothState(newState, bluetoothState.isEnabled())
				
				if (newState == BluetoothState.STATE_OFF) {
					stopScan()
				}
			}
	}
	
	fun updateBluetoothState(state: BluetoothState, isEnabled: Boolean) {
		bluetooth_state.visibility = View.VISIBLE
		bluetooth_state.setBackgroundColor(ContextCompat.getColor(this, state.bgColor))
		bluetooth_state.text = getString(state.text)
		
		val icon = AppCompatResources.getDrawable(this, if (!isEnabled) R.drawable.ic_round_bluetooth_24px else R.drawable.ic_round_bluetooth_disabled_24px)
			?.mutate()
		icon?.setColorFilter(ContextCompat.getColor(this, R.color.colorOnBackground), PorterDuff.Mode.SRC_IN)
		
		menu?.findItem(R.id.action_bluetooth)?.icon = icon
		
		// If the bluetooth is ON, we don't warn the user
		if (state == BluetoothState.STATE_ON) {
			bluetooth_state.visibility = View.GONE
		}
	}
	
	override fun onBeaconServiceConnect() {
		Timber.d("beaconManager is bound, ready to start scanning")
		beaconManager?.addRangeNotifier { beacons, _ ->
			if (isScanning) {
				storeBeaconsAround(beacons)
				logToWebhookIfNeeded()
			}
		}
		
		try {
			beaconManager?.startRangingBeaconsInRegion(Region("com.bridou_n.beaconscanner", null, null, null))
		} catch (e: RemoteException) {
			e.printStackTrace()
		}
	}
	
	private fun storeBeaconsAround(beacons: Collection<Beacon>) {
		loggingRequests.add(Observable.fromIterable(beacons)
			.map {
				val beaconInDb = try {
					db.beaconsDao().getBeaconById(it.hashCode()).blockingGet()
				} catch (e: EmptyResultSetException) {
					null
				}
				
				BeaconSaved.createFromBeacon(it, isBlocked = beaconInDb?.isBlocked ?: false)
			}
			.doOnNext {
				db.beaconsDao().insertBeacon(it)
			}
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({
				Timber.d("Beacon inserted")
			}, { err ->
				Timber.e(err)
				showGenericError(err?.message ?: "")
			}))
	}
	
	fun logToWebhookIfNeeded() {
		if (prefs.isLoggingEnabled && prefs.loggingEndpoint != null &&
			++numberOfScansSinceLog >= prefs.getLoggingFrequency()) {
			
			numberOfScansSinceLog = 0
			loggingRequests.add(db.beaconsDao().getBeaconsSeenAfter(prefs.lasLoggingCall)
				.filter { it.isNotEmpty() }
				.doOnSuccess { Timber.d("list to log: $it") }
				.map { LoggingRequest(prefs.loggingDeviceName ?: "", it) }
				.flatMapCompletable {
					loggingService.postLogs(prefs.loggingEndpoint ?: "", it)
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					Timber.d("Logged successfully")
					prefs.lasLoggingCall = Date().time
				}, { err ->
					Timber.e(IllegalStateException("Got err $err"))
				}))
		}
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			RC_COARSE_LOCATION -> {
				Timber.d("granted -> ${grantResults.hasGrantedPermission()}")
				
				if (grantResults.hasGrantedPermission()) {
					tracker.log("permission_granted", null)
					startScan()
				} else {
					tracker.log("permission_denied")
					
					Snackbar.make(root_view, getString(R.string.enable_permission_from_settings), Snackbar.LENGTH_INDEFINITE)
						.setAction(getString(R.string.enable)) {
							startActivity(Intent(
								Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
								Uri.parse("package:$packageName")
							).apply {
								addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
							})
						}.show()
				}
			}
		}
	}
	
	private fun showBluetoothNotEnabledError() {
		Snackbar.make(root_view, getString(R.string.enable_bluetooth_to_start_scanning), Snackbar.LENGTH_LONG)
			.setAction(getString(R.string.enable)) { _ ->
				bluetoothState.toggle()
				tracker.log("action_bluetooth")
			}
			.show()
	}
	
	fun showGenericError(msg: String) {
		Snackbar.make(root_view, msg, Snackbar.LENGTH_LONG).show()
	}
	
	private fun showLoggingError() = Snackbar.make(root_view, getString(R.string.logging_error_please_check), Snackbar.LENGTH_LONG).show()
	
	private fun showScanningState(state: Boolean) {
		toolbar.title = getString(if (state) R.string.scanning_for_beacons else R.string.app_name)
		progress_1.visibility = if (state) View.VISIBLE else View.GONE
		progress_2.visibility = if (state) View.VISIBLE else View.GONE
		
		scan_fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, if (state) R.color.colorPauseFab else R.color.colorSecondary))
		
		if (AndroidVersion.isLOrLater()) {
			val anim = AnimatedVectorDrawableCompat.create(this, if (state) R.drawable.play_to_pause else R.drawable.pause_to_play) as AnimatedVectorDrawableCompat
			
			scan_fab.setImageDrawable(anim)
			anim.start()
		} else {
			scan_fab.setImageDrawable(AppCompatResources.getDrawable(this, if (state) R.drawable.pause_icon else R.drawable.play_icon))
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main_menu, menu)
		
		this.menu = menu
		observeBluetoothState()
		return true
	}
	
	fun showClearDialog() {
		dialog?.dismiss()
		dialog = MaterialDialog(this)
			.title(R.string.delete_all)
			.message(R.string.are_you_sure_delete_all)
			.positiveButton(android.R.string.ok, click = {
				tracker.log("action_clear_accepted")
				loggingRequests.add(
					Completable.fromCallable {
						db.beaconsDao().clearBeacons()
					}.subscribeOn(Schedulers.io()).subscribe()
				)
			})
			.negativeButton(android.R.string.cancel)
		dialog?.show()
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_bluetooth -> {
				bluetoothState.toggle()
				tracker.log("action_bluetooth")
			}
			R.id.action_clear -> {
				tracker.log("action_clear")
				showClearDialog()
			}
			R.id.action_settings -> {
				tracker.log("action_settings")
				startActivity(Intent(this, SettingsActivity::class.java))
			}
			else -> return super.onOptionsItemSelected(item)
		}
		return true
	}
	
	override fun onPause() {
		prefs.setScanningState(isScanning())
		unbindBeaconManager()
		listQuery?.dispose()
		loggingRequests.clear()
		bluetoothStateDisposable?.dispose()
		keepScreenOn(false)
		super.onPause()
	}
	
	override fun onDestroy() {
		dialog?.dismiss()
		super.onDestroy()
	}
}
