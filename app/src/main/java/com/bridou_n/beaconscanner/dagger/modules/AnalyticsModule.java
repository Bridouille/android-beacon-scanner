package com.bridou_n.beaconscanner.dagger.modules;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bridou_n on 06/04/2017.
 */

@Module
public class AnalyticsModule {

    @Provides @Singleton
    public FirebaseAnalytics providesFirebaseAnalytics(Context ctx) {
        return FirebaseAnalytics.getInstance(ctx);
    }
}
