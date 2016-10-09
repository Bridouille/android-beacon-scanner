package com.bridou_n.beaconscanner.dagger;

import android.content.Context;

import com.bridou_n.beaconscanner.events.RxBus;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = {
        ContextModule.class,
        DatabaseModule.class,
        EventModule.class,
})
public interface AppComponent {
    Context context();
    Realm realm();
    RxBus rxBus();
}
