package com.bridou_n.beaconscanner

import android.app.Application
import android.util.Log.ERROR
import com.bridou_n.beaconscanner.dagger.components.ActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerActivityComponent
import com.bridou_n.beaconscanner.dagger.components.DaggerAppComponent
import com.bridou_n.beaconscanner.dagger.modules.*
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.crashlytics.android.Crashlytics
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
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

        // Timber
        Timber.plant(CrashReportingTree())

        // Realm
        Realm.init(this)

        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(realmConfig)

        ratingHelper.incrementAppOpens()
    }
}

/** A tree which logs important information for crash reporting.  */
class CrashReportingTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t) // Do the regular timber debug

        Crashlytics.log(priority, tag, message)

        t?.let {
            if (priority == ERROR) {
                Crashlytics.logException(t)
            }
        }
    }
}