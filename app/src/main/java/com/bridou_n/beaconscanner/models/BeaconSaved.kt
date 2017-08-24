package com.bridou_n.beaconscanner.models

import com.google.gson.annotations.SerializedName
import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * Created by bridou_n on 30/09/2016.
 */

open class BeaconSaved : RealmObject() {

    companion object {
        @Ignore const val TYPE_EDDYSTONE_UID = 0
        @Ignore const val TYPE_EDDYSTONE_URL = 1
        @Ignore const val TYPE_ALTBEACON = 2
        @Ignore const val TYPE_IBEACON = 3
    }

    @PrimaryKey
    @SerializedName("hashcode") var hashcode: Int = 0 // hashcode()
    @SerializedName("beaconType") var beaconType: Int = 0 // Eddystone, altBeacon, iBeacon
    @SerializedName("beaconAddress") var beaconAddress: String? = null // MAC address of the bluetooth emitter
    @SerializedName("uuid") var uuid: String? = null
    @SerializedName("major") var major: String? = null
    @SerializedName("minor") var minor: String? = null
    @SerializedName("txPower") var txPower: Int = 0
    @SerializedName("rssi") var rssi: Int = 0
    @SerializedName("distance") var distance: Double = 0.toDouble()
    @SerializedName("lastSeen") var lastSeen: Long = 0
    @SerializedName("lastMinuteSeen") var lastMinuteSeen: Long = 0
    @SerializedName("manufacturer") var manufacturer: Int = 0
    @SerializedName("url") var url: String? = null
    @SerializedName("namespaceId") var namespaceId: String? = null
    @SerializedName("instanceId") var instanceId: String? = null
    @SerializedName("hasTelemetryData") var hasTelemetryData: Boolean = false
    @SerializedName("telemetryVersion") var telemetryVersion: Long = 0
    @SerializedName("batteryMilliVolts") var batteryMilliVolts: Long = 0
    @SerializedName("temperature") private var temperature: Float = 0F

    @SerializedName("pduCount") var pduCount: Long = 0
    @SerializedName("uptime") var uptime: Long = 0

    fun getTemperature(): Float {
        val tmp = temperature / 256F

        if (tmp == (1 shl 7).toFloat()) { // 0x8000
            return 0F
        }
        return if (tmp > (1 shl 7)) tmp - (1 shl 8) else tmp
    }

    fun setTemperature(temperature: Float) {
        this.temperature = temperature
    }
}
