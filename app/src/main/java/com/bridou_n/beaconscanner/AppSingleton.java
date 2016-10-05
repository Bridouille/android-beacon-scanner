package com.bridou_n.beaconscanner;

import android.app.Application;

import com.bridou_n.beaconscanner.dagger.AnimationModule;
import com.bridou_n.beaconscanner.dagger.AppComponent;
import com.bridou_n.beaconscanner.dagger.BluetoothModule;
import com.bridou_n.beaconscanner.dagger.DaggerAppComponent;
import com.bridou_n.beaconscanner.dagger.DatabaseModule;
import com.bridou_n.beaconscanner.dagger.EventModule;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by bridou_n on 30/09/2016.
 */

public class AppSingleton extends Application {

    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);

        component = DaggerAppComponent.builder()
                        .databaseModule(new DatabaseModule())
                        .bluetoothModule(new BluetoothModule(this))
                        .eventModule(new EventModule())
                        .animationModule(new AnimationModule(this))
                        .build();
    }

    public static AppComponent component() {
        return component;
    }
}
