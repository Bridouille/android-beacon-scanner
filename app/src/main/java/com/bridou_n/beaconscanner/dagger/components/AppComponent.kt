package com.bridou_n.beaconscanner.dagger.components

import android.content.Context
import com.bridou_n.beaconscanner.dagger.modules.*
import com.bridou_n.beaconscanner.events.RxBus
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by bridou_n on 05/10/2016.
 */
@Singleton
@Component(modules = arrayOf(ContextModule::class, DatabaseModule::class, EventModule::class, PreferencesModule::class, AnalyticsModule::class))
interface AppComponent {
    fun context(): Context
    fun realm(): Realm
    fun rxBus(): RxBus
    fun prefs(): PreferencesHelper
    fun tracker(): FirebaseAnalytics
}
