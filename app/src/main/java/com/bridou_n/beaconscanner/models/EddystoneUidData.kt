package com.bridou_n.beaconscanner.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by bridou_n on 10/09/2017.
 */

open class EddystoneUidData() : RealmObject() {
    @SerializedName("namespaceId") var namespaceId: String? = null
    @SerializedName("instanceId") var instanceId: String? = null

    constructor(nameId: String, instId: String) : this() {
        namespaceId = nameId
        instanceId = instId
    }

    fun clone(): EddystoneUidData {
        val ret = EddystoneUidData()

        ret.namespaceId = namespaceId
        ret.instanceId = instanceId

        return ret
    }
}