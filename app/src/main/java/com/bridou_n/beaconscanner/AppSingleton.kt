package com.bridou_n.beaconscanner

import android.app.Application
import com.bridou_n.beaconscanner.dagger.components.ActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerAppComponent
import com.bridou_n.beaconscanner.dagger.modules.*
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by bridou_n on 30/09/2016.
 */

class AppSingleton : Application() {

    companion object {
        lateinit var activityComponent: ActivityComponent
    }

    override fun onCreate() {
        super.onCreate()

        val appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .databaseModule(DatabaseModule())
                .eventModule(EventModule())
                .build()

        activityComponent = DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .bluetoothModule(BluetoothModule())
                .build()

        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}
