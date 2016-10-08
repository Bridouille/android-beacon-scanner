package com.bridou_n.beaconscanner.dagger;

import com.bridou_n.beaconscanner.events.RxBus;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = {
        DatabaseModule.class,
        EventModule.class,
})
public interface AppComponent {
    Realm realm();
    RxBus rxBus();
}
