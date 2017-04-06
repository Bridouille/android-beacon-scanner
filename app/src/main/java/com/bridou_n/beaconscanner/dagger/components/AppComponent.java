package com.bridou_n.beaconscanner.dagger.components;

import android.content.Context;

import com.bridou_n.beaconscanner.dagger.modules.AnalyticsModule;
import com.bridou_n.beaconscanner.dagger.modules.ContextModule;
import com.bridou_n.beaconscanner.dagger.modules.DatabaseModule;
import com.bridou_n.beaconscanner.dagger.modules.EventModule;
import com.bridou_n.beaconscanner.dagger.modules.PreferencesModule;
import com.bridou_n.beaconscanner.events.RxBus;
import com.bridou_n.beaconscanner.utils.PreferencesHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

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
        PreferencesModule.class,
        AnalyticsModule.class
})
public interface AppComponent {
    Context context();
    Realm realm();
    RxBus rxBus();
    PreferencesHelper prefs();
    FirebaseAnalytics tracker();
}
