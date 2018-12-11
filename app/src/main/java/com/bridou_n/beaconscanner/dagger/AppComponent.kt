package com.bridou_n.beaconscanner.dagger

import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.features.beaconList.BeaconListActivity
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.features.blockedList.BlockedActivity
import com.bridou_n.beaconscanner.features.settings.SettingsActivity
import dagger.Component
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
    PreferencesModule::class,
    AnalyticsModule::class,
    BluetoothModule::class
])
interface AppComponent {
    fun providesBeaconManager() : BeaconManager

    fun inject(app: AppSingleton)
    fun inject(activity: BeaconListActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: BlockedActivity)

    fun inject(bs: ControlsBottomSheetDialog)
}
