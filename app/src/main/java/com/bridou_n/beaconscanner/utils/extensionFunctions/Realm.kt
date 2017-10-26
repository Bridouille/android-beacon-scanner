package com.bridou_n.beaconscanner.utils.extensionFunctions

import com.bridou_n.beaconscanner.models.BeaconSaved
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by bridou_n on 26/10/2017.
 */

fun Realm.getScannedBeacons(blocked: Boolean = false) : RealmResults<BeaconSaved> {
    return this.where(BeaconSaved::class.java)
            .equalTo("isBlocked", blocked)
            .findAllSortedAsync(arrayOf("lastMinuteSeen", "distance"), arrayOf(Sort.DESCENDING, Sort.ASCENDING))
}

fun Realm.getBeaconsScannedAfter(timestamp: Long) : RealmResults<BeaconSaved> {
    return this.where(BeaconSaved::class.java)
            .greaterThan("lastSeen", timestamp)
            .equalTo("isBlocked", false)
            .findAllAsync()
}

fun Realm.getBeaconWithId(hashcode: Int) : BeaconSaved? {
    return this.where(BeaconSaved::class.java).equalTo("hashcode", hashcode).findFirst()
}

fun Realm.clearScannedBeacons(blocked: Boolean = false) {
    this.executeTransactionAsync { tRealm ->
        tRealm.where(BeaconSaved::class.java)
                .equalTo("isBlocked", blocked)
                .findAll().deleteAllFromRealm()
    }
}

fun Realm.flushDb() {
    this.executeTransactionAsync { tRealm ->
        tRealm.deleteAll()
    }
}