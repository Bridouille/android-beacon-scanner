package com.bridou_n.beaconscanner.models

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
    var hashcode: Int = 0 // hashcode()
    var beaconType: Int = 0 // Eddystone, altBeacon, iBeacon
    var beaconAddress: String? = null // MAC address of the bluetooth emitter
    var uuid: String? = null
    var major: String? = null
    var minor: String? = null
    var txPower: Int = 0
    var rssi: Int = 0
    var distance: Double = 0.toDouble()
    var lastSeen: Date? = null
    var lastMinuteSeen: Long = 0
    var manufacturer: Int = 0
    var url: String? = null
    var namespaceId: String? = null
    var instanceId: String? = null
    var isHasTelemetryData: Boolean = false
    var telemetryVersion: Long = 0
    var batteryMilliVolts: Long = 0
    private var temperature: Float = 0F
    var pduCount: Long = 0
    var uptime: Long = 0

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
