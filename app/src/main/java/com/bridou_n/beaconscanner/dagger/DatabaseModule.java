package com.bridou_n.beaconscanner.dagger;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

/**
 * Created by bridou_n on 05/10/2016.
 */
@Module
public class DatabaseModule {
    @Provides
    public Realm providesRealm() {
        return Realm.getDefaultInstance();
    }
}
