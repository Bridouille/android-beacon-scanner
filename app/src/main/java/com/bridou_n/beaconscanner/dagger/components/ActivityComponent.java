package com.bridou_n.beaconscanner.dagger.components;

import com.bridou_n.beaconscanner.dagger.PerActivity;
import com.bridou_n.beaconscanner.dagger.modules.AnimationModule;
import com.bridou_n.beaconscanner.dagger.modules.BluetoothModule;
import com.bridou_n.beaconscanner.features.beaconList.MainActivity;
import com.bridou_n.beaconscanner.features.settings.SettingsActivity;

import dagger.Component;

/**
 * Created by bridou_n on 08/10/2016.
 */
@PerActivity
@Component(
    dependencies = AppComponent.class,
    modules = {
            BluetoothModule.class,
            AnimationModule.class
    })
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(SettingsActivity activity);
}
