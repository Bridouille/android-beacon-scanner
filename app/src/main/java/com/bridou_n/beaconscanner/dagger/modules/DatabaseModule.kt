package com.bridou_n.beaconscanner.dagger.modules

import dagger.Module
import dagger.Provides
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by bridou_n on 05/10/2016.
 */
@Module
class DatabaseModule {
    @Provides
    fun providesRealm(): Realm {
        return Realm.getDefaultInstance()
    }
}
