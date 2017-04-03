package com.bridou_n.beaconscanner;

import android.app.Application;

import com.bridou_n.beaconscanner.dagger.components.ActivityComponent;
import com.bridou_n.beaconscanner.dagger.modules.AnimationModule;
import com.bridou_n.beaconscanner.dagger.components.AppComponent;
import com.bridou_n.beaconscanner.dagger.modules.BluetoothModule;
import com.bridou_n.beaconscanner.dagger.modules.ContextModule;
import com.bridou_n.beaconscanner.dagger.DaggerActivityComponent;
import com.bridou_n.beaconscanner.dagger.DaggerAppComponent;
import com.bridou_n.beaconscanner.dagger.modules.DatabaseModule;
import com.bridou_n.beaconscanner.dagger.modules.EventModule;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by bridou_n on 30/09/2016.
 */

public class AppSingleton extends Application {

    private static AppComponent appComponent;
    private static ActivityComponent activityComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);

        appComponent = DaggerAppComponent.builder()
                        .contextModule(new ContextModule(this))
                        .databaseModule(new DatabaseModule())
                        .eventModule(new EventModule())
                        .build();

        activityComponent = DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .bluetoothModule(new BluetoothModule())
                .animationModule(new AnimationModule())
                .build();
    }

    public static AppComponent appComponent() {
        return appComponent;
    }

    public static ActivityComponent activityComponent() {
        return activityComponent;
    }
}
