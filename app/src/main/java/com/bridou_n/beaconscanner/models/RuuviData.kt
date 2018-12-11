package com.bridou_n.beaconscanner.models

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by bridou_n on 06/09/2017.
 */

data class RuuviData(
        /**
         * The air humidity in % age
         */
        @SerializedName("humidity")
        @ColumnInfo(name = "humidity")
        val humidity: Int = 0,

        /**
         * The airPressure in hPa
         */
        @SerializedName("air_pressure")
        @ColumnInfo(name = "air_pressure")
        val airPressure: Int = 0,

        /**
         * The temperature in C°
         */
        @SerializedName("temperature")
        @ColumnInfo(name = "temperature")
        val temperatue: Int = 0
)