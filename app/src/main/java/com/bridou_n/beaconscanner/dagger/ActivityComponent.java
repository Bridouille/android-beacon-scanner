package com.bridou_n.beaconscanner.dagger;

import com.bridou_n.beaconscanner.features.beaconList.MainActivity;

import dagger.Component;

/**
 * Created by bridou_n on 08/10/2016.
 */
@PerActivity
@Component(dependencies = AppComponent.class,
        modules = {
                BluetoothModule.class,
                AnimationModule.class
        })
public interface ActivityComponent {
        void inject(MainActivity activity);
}
