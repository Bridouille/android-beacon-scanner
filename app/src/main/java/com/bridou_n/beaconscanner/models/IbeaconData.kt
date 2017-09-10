package com.bridou_n.beaconscanner.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by bridou_n on 10/09/2017.
 */

open class IbeaconData() : RealmObject() {
    @SerializedName("uuid") var uuid: String? = null
    @SerializedName("major") var major: String? = null
    @SerializedName("minor") var minor: String? = null

    constructor(id1: String, id2: String, id3: String) : this() {
        uuid = id1
        major = id2
        minor = id3
    }

    fun clone() : IbeaconData {
        val ret = IbeaconData()

        ret.uuid = uuid
        ret.major = major
        ret.minor = minor

        return ret
    }
}