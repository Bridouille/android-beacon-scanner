package com.bridou_n.beaconscanner.dagger

import com.bridou_n.beaconscanner.events.RxBus
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by bridou_n on 05/10/2016.
 */

@Module
object EventModule {

    @JvmStatic @Provides @Singleton
    fun providesRxBus(): RxBus {
        return RxBus()
    }
}
