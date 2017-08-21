package com.bridou_n.beaconscanner.dagger.modules

import com.bridou_n.beaconscanner.events.RxBus

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * Created by bridou_n on 05/10/2016.
 */

@Module
class EventModule {
    @Provides
    @Singleton
    fun providesRxBus(): RxBus {
        return RxBus()
    }
}
