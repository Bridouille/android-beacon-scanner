package com.bridou_n.beaconscanner.dagger.components

import com.bridou_n.beaconscanner.dagger.PerActivity
import com.bridou_n.beaconscanner.dagger.modules.AnimationModule
import com.bridou_n.beaconscanner.dagger.modules.BluetoothModule
import com.bridou_n.beaconscanner.features.beaconList.MainActivity
import com.bridou_n.beaconscanner.features.settings.SettingsActivity

import dagger.Component

/**
 * Created by bridou_n on 08/10/2016.
 */
@PerActivity
@Component(
        dependencies = arrayOf(AppComponent::class),
        modules = arrayOf(BluetoothModule::class, AnimationModule::class)
)
interface ActivityComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: SettingsActivity)
}
