package com.bridou_n.beaconscanner.models

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by bridou_n on 06/09/2017.
 */

data class TelemetryData(

    /**
     * A numeric version of the version of the telemetry format.
     * This is currently always 0, as this is the first version of the telemetry format
     */
    @SerializedName("version")
    @ColumnInfo(name = "version")
    val version: Long = 0,

    /**
     * A two byte indicator of the voltage of the battery on the beacon.
     * If the beacon does not have a battery (e.g. a USB powered beacon), this field is set to zero
     */
    @SerializedName("battery_milli_volts")
    @ColumnInfo(name = "battery_milli_volts")
    val batteryMilliVolts: Long = 0,

    /**
     * A two byte field indicating the output of a temperature sensor on the beacon, if supported by the hardware.
     * Note, however, that beacon temperature sensors are often pretty inaccurate, and can be influenced by heating of adjacent electronic components.
     */
    @SerializedName("temperature")
    @ColumnInfo(name = "temperature")
    val temperature: Float = 0F,

    /**
     * A count of how many advertising packets have been transmitted by the beacon since it was last powered on
     */
    @SerializedName("pdu_count")
    @ColumnInfo(name = "pdu_count")
    val pduCount: Long = 0,

    /**
     * A four byte measurement of how many seconds the beacon has been powered.
     * Since most beacons are based on low-power hardware that do not contain
     */
    @SerializedName("uptime_seconds")
    @ColumnInfo(name = "uptime_seconds")
    val uptime: Long = 0
) {

    companion object {
        fun getTemperatureFromTlmField(tmp: Float): Float {
            val ret = tmp / 256F

            if (ret == (1 shl 7).toFloat()) { // 0x8000
                return 0F
            }
            return if (ret > (1 shl 7)) ret - (1 shl 8) else ret
        }
    }
}

