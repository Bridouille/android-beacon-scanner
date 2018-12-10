package com.bridou_n.beaconscanner.utils.extensionFunctions

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by bridou_n on 26/10/2017.
 */

fun FirebaseAnalytics.log(name: String, bundle: Bundle? = null) {
    logEvent(name, null)
}

fun FirebaseAnalytics.logBeaconScanned(manufacturer: Int, type: String, distance: Double) {
    log("adding_or_updating_beacon", Bundle().apply {
        putInt("manufacturer", manufacturer)
        putString("type", type)
        putDouble("distance", distance)
    })
}