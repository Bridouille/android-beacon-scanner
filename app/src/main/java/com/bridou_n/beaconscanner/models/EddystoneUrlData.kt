package com.bridou_n.beaconscanner.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by bridou_n on 10/09/2017.
 */

open class EddystoneUrlData() : RealmObject() {
    @SerializedName("url") var url: String? = null

    constructor(u: String) : this() {
        url = u
    }

    fun clone(): EddystoneUrlData {
        val ret = EddystoneUrlData()

        ret.url = url

        return ret
    }
}