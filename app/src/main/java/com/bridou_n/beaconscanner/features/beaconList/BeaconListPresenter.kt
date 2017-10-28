package com.bridou_n.beaconscanner.features.beaconList

import android.os.RemoteException
import android.util.Log
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.events.Events
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.models.LoggingRequest
import com.bridou_n.beaconscanner.utils.BluetoothManager
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.*
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by bridou_n on 22/08/2017.
 */

class BeaconListPresenter(val view: BeaconListContract.View,
                          val rxBus: RxBus,
                          val prefs: PreferencesHelper,
                          val realm: Realm,
                          val loggingService: LoggingService,
                          val bluetoothState: BluetoothManager,
                          val ratingHelper: RatingHelper,
                          val tracker: FirebaseAnalytics) : BeaconListContract.Presenter {

    private val TAG = "BeaconListPresenter"

    private lateinit var beaconResults: RealmResults<BeaconSaved>

    private var bluetoothStateDisposable: Disposable? = null
    private var rangeDisposable: Disposable? = null
    private var beaconManager: BeaconManager? = null

    private var numberOfScansSinceLog = 0
    private val MAX_RETRIES = 3
    private var loggingRequests = CompositeDisposable()

    override fun setBeaconManager(bm: BeaconManager) {
        beaconManager = bm
    }

    override fun start() {
        // Setup an observable on the bluetooth changes
        bluetoothStateDisposable = bluetoothState.asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { e ->
                    if (e is Events.BluetoothState) {
                        view.updateBluetoothState(e.getBluetoothState(), bluetoothState.isEnabled)

                        if (e.getBluetoothState() == BeaconListActivity.BluetoothState.STATE_OFF) {
                            stopScan()
                        }
                    }
                }

        beaconResults = realm.getScannedBeacons()
        view.setAdapter(beaconResults)

        beaconResults.addChangeListener { results ->
            if (results.isLoaded) {
                view.showEmptyView(results.size == 0)
            }
        }

        // Show the tutorial if needed
        if (!prefs.hasSeenTutorial()) {
            prefs.setHasSeenTutorial(view.showTutorial())
        }

        // Start scanning if the scan on open is activated or if we were previously scanning
        if (prefs.isScanOnOpen || prefs.wasScanning()) {
            startScan()
        }
    }

    override fun toggleScan() {
        if (!isScanning()) {
            tracker.logEvent("start_scanning_clicked", null)
            return startScan()
        }
        tracker.logEvent("stop_scanning_clicked", null)
        stopScan()
    }

    override fun startScan() {
        if (!view.hasCoarseLocationPermission()) {
            return view.askForCoarseLocationPermission()
        }

        if (!bluetoothState.isEnabled || beaconManager == null) {
            return view.showBluetoothNotEnabledError()
        }

        if (!(beaconManager?.isBound(view) ?: false)) {
            Log.d(TAG, "binding beaconManager")
            beaconManager?.bind(view)
        }

        if (prefs.preventSleep) {
            view.keepScreenOn(true)
        }

        view.showScanningState(true)
        rangeDisposable?.dispose() // clear the previous subscription if any
        rangeDisposable = rxBus.asFlowable() // Listen for range events
                .observeOn(AndroidSchedulers.mainThread()) // We use this so we use the realm on the good thread & we can make UI changes
                .filter({ e -> e is Events.RangeBeacon && e.beacons.isNotEmpty() })
                .subscribe({ e ->
                    e as Events.RangeBeacon

                    handleRating()
                    storeBeaconsAround(e.beacons)
                    logToWebhookIfNeeded()
                }, { err ->
                    view.showGenericError(err.message ?: "")
                })
    }

    override fun onBeaconServiceConnect() {
        Log.d(TAG, "beaconManager is bound, ready to start scanning")
        beaconManager?.addRangeNotifier { beacons, region -> rxBus.send(Events.RangeBeacon(beacons, region)) }

        try {
            beaconManager?.startRangingBeaconsInRegion(Region("com.bridou_n.beaconscanner", null, null, null))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onLocationPermissionGranted() {
        tracker.logEvent("permission_granted", null)
        startScan()
    }

    override fun onLocationPermissionDenied(requestCode: Int, permList: List<String>) {
        tracker.logEvent("permission_denied")

        // If the user refused the permission, we just disabled the scan on open
        prefs.isScanOnOpen = false
        if (view.hasSomePermissionPermanentlyDenied(permList)) {
            tracker.logEvent("permission_denied_permanently")
            view.showEnablePermissionSnackbar()
        }
    }

    fun handleRating() {
        if (ratingHelper.shouldShowRatingRationale()) {
            ratingHelper.setRatingOngoing()
            view.showRating(RatingHelper.STEP_ONE)
        }
    }

    override fun onRatingInteraction(step: Int, answer: Boolean) {
        Log.d(TAG, "step: $step -- answer : $answer")
        if (!answer) { // The user answered "no" to any question
            return view.showRating(step, false)
        }

        when (step) {
            RatingHelper.STEP_ONE -> view.showRating(RatingHelper.STEP_TWO)
            RatingHelper.STEP_TWO -> {
                ratingHelper.setPopupSeen()
                view.redirectToStorePage()
                view.showRating(step, false)
            }
        }
    }

    override fun storeBeaconsAround(beacons: Collection<Beacon>) {
        realm.executeTransactionAsync({ tRealm ->
            for (b: Beacon in beacons) {
                val beacon = BeaconSaved(b) // Create a new object

                val res = tRealm.getBeaconWithId(beacon.hashcode) // See if we scanned this beacon before

                res?.let {  // If we did, update the beacon logic fields
                    beacon.isBlocked = it.isBlocked
                }

                tRealm.copyToRealmOrUpdate(beacon)
                tracker.logBeaconScanned(beacon.manufacturer, beacon.beaconType, beacon.distance)
            }
        }, null, { error: Throwable? ->
            view.showGenericError(error?.message ?: "")
        })
    }

    fun logToWebhookIfNeeded() {
        if (prefs.isLoggingEnabled && prefs.loggingEndpoint != null &&
                ++numberOfScansSinceLog >= prefs.getLoggingFrequency()) {
            val beaconToLog = realm.getBeaconsScannedAfter(prefs.lasLoggingCall)

            numberOfScansSinceLog = 0 // Reset the counter before we get the results
            beaconToLog.addChangeListener { results ->
                if (results.isLoaded && results.isNotEmpty()) {
                    Log.d(TAG, "Result is loaded size : ${results.size} - lastLoggingCall : ${Date(prefs.lasLoggingCall)}")

                    // Execute the network request
                    prefs.lasLoggingCall = Date().time

                    // We clone the objects
                    val resultPlainObjects = results.map { it.clone() }
                    val req = LoggingRequest(prefs.loggingDeviceName ?: "", resultPlainObjects)

                    loggingRequests.add(loggingService.postLogs(prefs.loggingEndpoint ?: "", req)
                            .retryWhen({ errors: Flowable<Throwable> ->
                                errors.zipWith(Flowable.range(1, MAX_RETRIES + 1), BiFunction { _: Throwable, attempt: Int ->
                                    Log.d(TAG, "attempt : $attempt")
                                    if (attempt > MAX_RETRIES) {
                                        view.showLoggingError()
                                    }
                                    attempt
                                }).flatMap { attempt ->
                                    if (attempt > MAX_RETRIES) {
                                        Flowable.empty()
                                    } else {
                                        Flowable.timer(Math.pow(4.0, attempt.toDouble()).toLong(), TimeUnit.SECONDS)
                                    }
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe())

                    beaconToLog.removeAllChangeListeners()
                }
            }
        }
    }

    override fun stopScan() {
        unbindBeaconManager()
        rangeDisposable?.dispose()
        view.showScanningState(false)
        view.keepScreenOn(false)
    }

    override fun onBluetoothToggle() {
        bluetoothState.toggle()
        tracker.logEvent("action_bluetooth")
    }

    override fun onSettingsClicked() {
        tracker.logEvent("action_settings")
        view.startSettingsActivity()
    }

    override fun onClearClicked() {
        tracker.logEvent("action_clear")
        view.showClearDialog()
    }

    override fun onClearAccepted() {
        tracker.logEvent("action_clear_accepted")
        realm.clearScannedBeacons()
    }

    fun isScanning() = !(rangeDisposable?.isDisposed ?: true)

    override fun stop() {
        prefs.setScanningState(isScanning())
        unbindBeaconManager()
        beaconResults.removeAllChangeListeners()
        loggingRequests.clear()
        bluetoothStateDisposable?.dispose()
        rangeDisposable?.dispose()
        view.keepScreenOn(false)
    }

    fun unbindBeaconManager() {
        if (beaconManager?.isBound(view) == true) {
            Log.d(TAG, "Unbinding from beaconManager")
            beaconManager?.unbind(view)
        }
    }

    override fun clear() {
        realm.close()
    }
}