package com.bridou_n.beaconscanner.dagger

import android.content.Context
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by bridou_n on 03/04/2017.
 */

@Module
object PreferencesModule {

    @JvmStatic @Provides @Singleton
    fun providesPreferencesHelper(ctx: Context) = PreferencesHelper(ctx)
}
