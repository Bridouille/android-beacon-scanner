package com.bridou_n.beaconscanner

import android.app.Application
import com.bridou_n.beaconscanner.dagger.components.ActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerAppComponent
import com.bridou_n.beaconscanner.dagger.modules.*
import com.bridou_n.beaconscanner.utils.RatingHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

/**
 * Created by bridou_n on 30/09/2016.
 */

class AppSingleton : Application() {

    companion object {
        lateinit var activityComponent: ActivityComponent
    }

    @Inject lateinit var ratingHelper: RatingHelper

    override fun onCreate() {
        super.onCreate()

        val appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .networkModule(NetworkModule())
                .databaseModule(DatabaseModule())
                .eventModule(EventModule())
                .build()

        activityComponent = DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .bluetoothModule(BluetoothModule())
                .build()

        activityComponent.inject(this)

        Realm.init(this)

        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(realmConfig)

        ratingHelper.incrementAppOpens()
    }
}
