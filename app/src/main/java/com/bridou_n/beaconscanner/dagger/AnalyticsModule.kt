package com.bridou_n.beaconscanner.dagger

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by bridou_n on 06/04/2017.
 */

@Module
object AnalyticsModule {

    @JvmStatic @Provides @Singleton
    fun providesFirebaseAnalytics(ctx: Context) = FirebaseAnalytics.getInstance(ctx)
}
