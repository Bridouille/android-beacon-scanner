package com.bridou_n.beaconscanner.utils.extensionFunctions

import com.bridou_n.beaconscanner.utils.CountHelper

/**
 * Created by bridou_n on 05/09/2017.
 */

fun IntArray.print() : String {
    val output = StringBuilder("[")

    for (i in 0..this.size - 1) {
        output.append(this[i].toString())
        if (i < this.size - 1) {
            output.append(",")
        }
    }

    output.append("]")
    return output.toString()
}

fun Long.toCoolFormat() : String {
    return CountHelper.coolFormat(this.toDouble(), 0)
}