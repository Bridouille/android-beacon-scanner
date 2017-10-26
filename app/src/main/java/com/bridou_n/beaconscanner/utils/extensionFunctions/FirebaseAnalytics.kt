package com.bridou_n.beaconscanner.utils.extensionFunctions

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by bridou_n on 26/10/2017.
 */

fun FirebaseAnalytics.logEvent(name: String) {
    logEvent(name, null)
}

fun FirebaseAnalytics.logBeaconScanned(manufacturer: Int, type: String, distance: Double) {
    val infos = Bundle()
    infos.putInt("manufacturer", manufacturer)
    infos.putString("type", type)
    infos.putDouble("distance", distance)

    logEvent("adding_or_updating_beacon", infos)
}