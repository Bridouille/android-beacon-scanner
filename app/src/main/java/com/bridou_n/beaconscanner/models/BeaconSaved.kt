package com.bridou_n.beaconscanner.models

import android.text.format.DateUtils
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.bridou_n.beaconscanner.Database.BeaconsDao
import com.bridou_n.beaconscanner.utils.BuildTypes
import com.bridou_n.beaconscanner.utils.RuuviParser
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor
import java.lang.IllegalStateException
import java.util.*

/**
 * Created by bridou_n on 30/09/2016.
 */

@Entity(
        tableName = BeaconsDao.TABLE_NAME,
        primaryKeys = [
            "hashcode"
        ]
)
data class BeaconSaved(
    @SerializedName("hashcode")
    @ColumnInfo(name = "hashcode")
    val hashcode: Int = 0, // hashcode()

    @SerializedName("beacon_type")
    @ColumnInfo(name = "beacon_type")
    val beaconType: String? = null, // Eddystone, altBeacon, iBeacon

    @SerializedName("beacon_address")
    @ColumnInfo(name = "beacon_address")
    val beaconAddress: String? = null, // MAC address of the bluetooth emitter

    @SerializedName("manufacturer")
    @ColumnInfo(name = "manufacturer")
    val manufacturer: Int = 0,

    @SerializedName("tx_power")
    @ColumnInfo(name = "tx_power")
    val txPower: Int = 0,

    @SerializedName("rssi")
    @ColumnInfo(name = "rssi")
    val rssi: Int = 0,

    @SerializedName("distance")
    @ColumnInfo(name = "distance")
    val distance: Double = 0.toDouble(),

    @SerializedName("last_seen")
    @ColumnInfo(name = "last_seen")
    val lastSeen: Long = 0,

    /**
     * Specialized field for every beacon type
     */
    @SerializedName("ibeacon_data")
    @Embedded(prefix = "ibeacon_data_")
    val ibeaconData: IbeaconData? = null,

    @SerializedName("eddystone_url_data")
    @Embedded(prefix = "eddystone_url_data_")
    val eddystoneUrlData: EddystoneUrlData? = null,

    @SerializedName("eddystoneUidData")
    @Embedded(prefix = "eddystone_uid_data_")
    val eddystoneUidData: EddystoneUidData? = null,

    @SerializedName("telemetry_data")
    @Embedded(prefix = "telemetry_data_")
    val telemetryData: TelemetryData? = null,

    @SerializedName("ruuvi_data")
    @Embedded(prefix = "ruuvi_data_")
    val ruuviData: RuuviData? = null,

    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean = false,

    @ColumnInfo(name="is_white")
    val isWhite: Boolean = false

) {
    companion object {
        const val TYPE_EDDYSTONE_UID = "eddystone_uid"
        const val TYPE_EDDYSTONE_URL = "eddystone_url"
        const val TYPE_ALTBEACON = "altbeacon"
        const val TYPE_IBEACON = "ibeacon"
        const val TYPE_RUUVITAG = "ruuvitag"

        fun createFromBeacon(beacon: Beacon, isBlocked: Boolean = false, isWhite: Boolean = false) : BeaconSaved {
            // Common fields to every beacons
            var hashcode = beacon.hashCode()
            val lastSeen = Date().time
            val beaconAddress = beacon.bluetoothAddress
            val manufacturer = beacon.manufacturer
            val rssi = beacon.rssi
            val txPower = beacon.txPower
            val distance = if (beacon.distance.isInfinite()) {
                (-1).toDouble()
            } else {
                beacon.distance
            }

            var beaconType: String? = null
            var ibeaconData: IbeaconData? = null
            var eddystoneUrlData: EddystoneUrlData? = null
            var eddystoneUidData: EddystoneUidData? = null
            var telemetryData: TelemetryData? = null
            var ruuviData: RuuviData? = null

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
                        beaconType = TYPE_EDDYSTONE_UID
                        eddystoneUidData = EddystoneUidData(beacon.id1.toString(), beacon.id2.toString())
                    }
                    0x10 -> { // This is a Eddystone-URL frame
                        beaconType = TYPE_EDDYSTONE_URL
                        val url = UrlBeaconUrlCompressor.uncompress(beacon.id1.toByteArray())
                        eddystoneUrlData = EddystoneUrlData(url)

                        if (url?.startsWith("https://ruu.vi/#") == true) { // This is a RuuviTag
                            val hash = url.split("#").get(1)

                            // We manually set the hashcode of the RuuviTag so it only appears once per address
                            hashcode = beaconAddress?.hashCode() ?: -1
                            beaconType = TYPE_RUUVITAG
                            val ruuviParser = RuuviParser(hash)

                            ruuviData = RuuviData(ruuviParser.humidity, ruuviParser.airPressure, ruuviParser.temp)
                        }
                    }
                }
            } else { // This is an iBeacon or ALTBeacon
                beaconType = if (beacon.beaconTypeCode == 0xBEAC) TYPE_ALTBEACON else TYPE_IBEACON // 0x4c000215 is iBeacon
                ibeaconData = IbeaconData(beacon.id1.toString(), beacon.id2.toString(), beacon.id3.toString())
            }

            return BeaconSaved(
                    hashcode = hashcode,
                    lastSeen = lastSeen,
                    manufacturer = manufacturer,
                    rssi = rssi,
                    txPower = txPower,
                    distance = distance,
                    beaconType = beaconType,
                    ibeaconData = ibeaconData,
                    eddystoneUrlData = eddystoneUrlData,
                    eddystoneUidData = eddystoneUidData,
                    telemetryData = telemetryData,
                    ruuviData = ruuviData,
                    isBlocked = isBlocked,
                    isWhite = isWhite
            )
        }
        
        fun eddystoneUidSample() : BeaconSaved {
            check(!BuildTypes.isRelease()) { "Only use this for debugging purposes" }
            return BeaconSaved(
                hashcode = UUID.randomUUID().hashCode(),
                beaconType = TYPE_EDDYSTONE_UID,
                beaconAddress = "74:FC:B0:45:0B:02",
                manufacturer = 0xFEAA,
                txPower = -63,
                rssi = -38,
                distance = 0.02,
                lastSeen = Date().time - 40 * DateUtils.SECOND_IN_MILLIS,
                eddystoneUidData = EddystoneUidData(
                    namespaceId = "0x00010203040506070809",
                    instanceId = "0xabcdefabcdef"
                ),
                telemetryData = TelemetryData(
                    batteryMilliVolts = 400,
                    temperature = 40.7F,
                    pduCount = 654,
                    uptime = 32
                )
            )
        }
    
        fun eddystoneUrlSample() : BeaconSaved {
            check(!BuildTypes.isRelease()) { "Only use this for debugging purposes" }
            return BeaconSaved(
                hashcode = UUID.randomUUID().hashCode(),
                beaconType = TYPE_EDDYSTONE_URL,
                beaconAddress = "74:FC:B0:45:0B:02",
                manufacturer = 0xFEAA,
                txPower = -63,
                rssi = -38,
                distance = 0.02,
                lastSeen = Date().time - 40 * DateUtils.SECOND_IN_MILLIS,
                eddystoneUrlData = EddystoneUrlData(
                    url = "http://example.com"
                ),
                telemetryData = TelemetryData(
                    batteryMilliVolts = 500,
                    temperature = 42.5F,
                    pduCount = 211345,
                    uptime = 5234
                )
            )
        }
    
        fun iBeaconSample() : BeaconSaved {
            check(!BuildTypes.isRelease()) { "Only use this for debugging purposes" }
            return BeaconSaved(
                hashcode = UUID.randomUUID().hashCode(),
                beaconType = TYPE_IBEACON,
                beaconAddress = "74:FC:B0:45:0B:02",
                manufacturer = 0x004C,
                txPower = -63,
                rssi = -38,
                distance = 0.02,
                lastSeen = Date().time - 40 * DateUtils.SECOND_IN_MILLIS,
                ibeaconData = IbeaconData(
                    uuid = UUID.randomUUID().toString(),
                    major = "1",
                    minor = "602"
                )
            )
        }
    
        fun altBeaconSample() : BeaconSaved {
            check(!BuildTypes.isRelease()) { "Only use this for debugging purposes" }
            return BeaconSaved(
                hashcode = UUID.randomUUID().hashCode(),
                beaconType = TYPE_ALTBEACON,
                beaconAddress = "74:FC:B0:45:0B:02",
                manufacturer = 0x0118,
                txPower = -63,
                rssi = -38,
                distance = 0.02,
                lastSeen = Date().time - 40 * DateUtils.SECOND_IN_MILLIS,
                ibeaconData = IbeaconData(
                    uuid = UUID.randomUUID().toString(),
                    major = "2",
                    minor = "342"
                )
            )
        }
    
        fun ruuviTagSample() : BeaconSaved {
            check(!BuildTypes.isRelease()) { "Only use this for debugging purposes" }
            return BeaconSaved(
                hashcode = UUID.randomUUID().hashCode(),
                beaconType = TYPE_RUUVITAG,
                beaconAddress = "77:CC:B7:45:0B:02",
                manufacturer = 0xFEAA,
                txPower = -63,
                rssi = -38,
                distance = 0.02,
                lastSeen = Date().time - 40 * DateUtils.SECOND_IN_MILLIS,
                ruuviData = RuuviData(
                    humidity = 66,
                    airPressure = 722,
                    temperatue = 22
                )
            )
        }
    }

    fun toJson(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}
