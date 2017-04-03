package com.bridou_n.beaconscanner.dagger.modules;

import android.content.Context;

import com.bridou_n.beaconscanner.utils.PreferencesHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bridou_n on 03/04/2017.
 */

@Module
public class PreferencesModule {

    @Provides @Singleton
    public PreferencesHelper providesPreferencesHelper(Context ctx) {
        return new PreferencesHelper(ctx);
    }
}
