package com.bridou_n.beaconscanner.dagger;

import com.bridou_n.beaconscanner.events.RxBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bridou_n on 05/10/2016.
 */

@Module
public class EventModule {
    @Provides @Singleton
    public RxBus providesRxBus() {
        return new RxBus();
    }
}
