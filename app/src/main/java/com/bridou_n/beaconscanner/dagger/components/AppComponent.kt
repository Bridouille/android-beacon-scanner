package com.bridou_n.beaconscanner.dagger.components

import android.content.Context
import com.bridou_n.beaconscanner.API.LoggingService
import com.bridou_n.beaconscanner.dagger.modules.*
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import io.realm.Realm
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = arrayOf(
        ContextModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        EventModule::class,
        PreferencesModule::class,
        AnalyticsModule::class
))
interface AppComponent {
    fun context(): Context
    fun realm(): Realm
    fun loggingService(): LoggingService
    fun rxBus(): RxBus
    fun prefs(): PreferencesHelper
    fun rating(): RatingHelper
    fun tracker(): FirebaseAnalytics
}
