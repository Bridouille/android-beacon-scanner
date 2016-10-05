package com.bridou_n.beaconscanner.dagger;

import com.bridou_n.beaconscanner.features.beaconList.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = {
        DatabaseModule.class,
        BluetoothModule.class,
        EventModule.class,
        AnimationModule.class
})
public interface AppComponent {
    void inject(MainActivity activity);
}
