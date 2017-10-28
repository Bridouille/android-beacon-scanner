package com.bridou_n.beaconscanner.dagger.components

import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.dagger.PerActivity
import com.bridou_n.beaconscanner.dagger.modules.BluetoothModule
import com.bridou_n.beaconscanner.features.beaconList.BeaconListActivity
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.features.blockedList.BlockedActivity
import com.bridou_n.beaconscanner.features.settings.SettingsActivity

import dagger.Component
import org.altbeacon.beacon.BeaconManager

/**
 * Created by bridou_n on 08/10/2016.
 */
@PerActivity
@Component(
        dependencies = arrayOf(AppComponent::class),
        modules = arrayOf(BluetoothModule::class)
)
interface ActivityComponent {
    fun providesBeaconManager() : BeaconManager

    fun inject(app: AppSingleton)
    fun inject(activity: BeaconListActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: BlockedActivity)

    fun inject(bs: ControlsBottomSheetDialog)
}
