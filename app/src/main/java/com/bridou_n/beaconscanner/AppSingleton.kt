package com.bridou_n.beaconscanner

import android.app.Application
import android.util.Log.ERROR
import com.bridou_n.beaconscanner.dagger.AppComponent
import com.bridou_n.beaconscanner.dagger.ContextModule
import com.bridou_n.beaconscanner.dagger.DaggerAppComponent
import com.bridou_n.beaconscanner.utils.BuildTypes
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by bridou_n on 30/09/2016.
 */

class AppSingleton : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    @Inject lateinit var ratingHelper: RatingHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        // Dagger
        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .build()
        appComponent.inject(this)

        // Timber
        Timber.plant(CrashReportingTree())

        // Analytics
        tracker.setAnalyticsCollectionEnabled(BuildTypes.isRelease())

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