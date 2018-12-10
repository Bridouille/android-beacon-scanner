package com.bridou_n.beaconscanner.dagger

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.features.beaconList.BeaconListActivity
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.features.blockedList.BlockedActivity
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import io.realm.Realm
import org.altbeacon.beacon.BeaconManager
import javax.inject.Singleton

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = [
    ContextModule::class,
    DatabaseModule::class,
    NetworkModule::class,
    EventModule::class,
    PreferencesModule::class,
    AnalyticsModule::class,
    BluetoothModule::class
])
interface AppComponent {
    /*fun context(): Context
    fun realm(): Realm
    fun loggingService(): LoggingService
    fun rxBus(): RxBus
    fun prefs(): PreferencesHelper
    fun rating(): RatingHelper
    fun tracker(): FirebaseAnalytics
    fun bluetoothAdapter(): BluetoothAdapter */

    fun providesBeaconManager() : BeaconManager

    fun inject(app: AppSingleton)
    fun inject(activity: BeaconListActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: BlockedActivity)

    fun inject(bs: ControlsBottomSheetDialog)
}
