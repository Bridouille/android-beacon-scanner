package com.bridou_n.beaconscanner.models

import com.bridou_n.beaconscanner.utils.RuuviParser
import com.google.gson.annotations.SerializedName
import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor

/**
 * Created by bridou_n on 30/09/2016.
 */

open class BeaconSaved() : RealmObject() {

    companion object {
        @Ignore const val TYPE_EDDYSTONE_UID = "eddystone_uid"
        @Ignore const val TYPE_EDDYSTONE_URL = "eddystone_url"
        @Ignore const val TYPE_ALTBEACON = "altbeacon"
        @Ignore const val TYPE_IBEACON = "ibeacon"
        @Ignore const val TYPE_RUUVITAG = "ruuvitag"
    }

    @PrimaryKey
    @SerializedName("hashcode") var hashcode: Int = 0 // hashcode()
    @SerializedName("beaconType") var beaconType: String = "" // Eddystone, altBeacon, iBeacon
    @SerializedName("beaconAddress") var beaconAddress: String? = null // MAC address of the bluetooth emitter
    @SerializedName("manufacturer") var manufacturer: Int = 0
    @SerializedName("txPower") var txPower: Int = 0
    @SerializedName("rssi") var rssi: Int = 0
    @SerializedName("distance") var distance: Double = 0.toDouble()
    @SerializedName("lastSeen") var lastSeen: Long = 0
    @SerializedName("lastMinuteSeen") var lastMinuteSeen: Long = 0

    @SerializedName("uuid") var uuid: String? = null
    @SerializedName("major") var major: String? = null
    @SerializedName("minor") var minor: String? = null

    @SerializedName("url") var url: String? = null
    @SerializedName("namespaceId") var namespaceId: String? = null
    @SerializedName("instanceId") var instanceId: String? = null

    @SerializedName("telemetryData") var telemetryData: TelemetryData? = null
    @SerializedName("ruuviData") var ruuviData: RuuviData? = null

    constructor(beacon: Beacon) : this() {
        // Common fields to every beacons
        hashcode = beacon.hashCode()
        lastSeen = Date().time
        lastMinuteSeen = Date().time / 1000 / 60
        beaconAddress = beacon.bluetoothAddress
        manufacturer = beacon.manufacturer
        rssi = beacon.rssi
        txPower = beacon.txPower
        distance = beacon.distance

        if (beacon.serviceUuid == 0xFEAA) { // This is an Eddystone format

            // Do we have telemetry data?
            if (beacon.extraDataFields.size >= 5) {
                telemetryData = TelemetryData(beacon.extraDataFields[0],
                        beacon.extraDataFields[1],
                        TelemetryData.getTemperatureFromTlmField(beacon.extraDataFields[2].toFloat()),
                        beacon.extraDataFields[3],
                        beacon.extraDataFields[4])
            }

            when (beacon.beaconTypeCode) {
                0x00 -> { // This is a Eddystone-UID frame
                    beaconType = BeaconSaved.TYPE_EDDYSTONE_UID
                    namespaceId = beacon.id1.toString()
                    instanceId = beacon.id2.toString()
                }
                0x10 -> { // This is a Eddystone-URL frame
                    beaconType = BeaconSaved.TYPE_EDDYSTONE_URL
                    url = UrlBeaconUrlCompressor.uncompress(beacon.id1.toByteArray())

                    if (url?.startsWith("https://ruu.vi/#") ?: false) { // This is a RuuviTag
                        val hash = url?.split("#")?.get(1)

                        if (hash != null) {
                            beaconType = BeaconSaved.TYPE_RUUVITAG
                            val ruuviParser = RuuviParser(hash)

                            ruuviData = RuuviData(ruuviParser.humidity, ruuviParser.airPressure, ruuviParser.temp)
                        }
                    }
                }
            }
        } else { // This is an iBeacon or ALTBeacon
            beaconType = if (beacon.beaconTypeCode == 0xBEAC) BeaconSaved.TYPE_ALTBEACON else BeaconSaved.TYPE_IBEACON // 0x4c000215 is iBeacon
            uuid = beacon.id1.toString()
            major = beacon.id2.toString()
            minor = beacon.id3.toString()
        }
    }

    fun clone() : BeaconSaved {
        val ret = BeaconSaved()

        ret.hashcode = hashcode
        ret.beaconType = beaconType
        ret.beaconAddress = beaconAddress
        ret.manufacturer = manufacturer
        ret.txPower = txPower
        ret.rssi = rssi
        ret.distance = distance
        ret.lastSeen = lastSeen
        ret.lastMinuteSeen = lastMinuteSeen

        ret.uuid = uuid
        ret.major = major
        ret.minor = minor

        ret.url = url
        ret.namespaceId = namespaceId
        ret.instanceId = instanceId

        ret.telemetryData = telemetryData?.clone()
        ret.ruuviData = ruuviData?.clone()

        return ret
    }
}
