package com.bridou_n.beaconscanner.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    private final Application context;

    public ContextModule(Application app) {
        context = app;
    }

    @Provides @Singleton
    public Context providesContext() {
        return context;
    }
}
