package com.bridou_n.beaconscanner.dagger.modules

import android.content.Context

import com.bridou_n.beaconscanner.utils.PreferencesHelper

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * Created by bridou_n on 03/04/2017.
 */

@Module
class PreferencesModule {

    @Provides
    @Singleton
    fun providesPreferencesHelper(ctx: Context): PreferencesHelper {
        return PreferencesHelper(ctx)
    }
}
