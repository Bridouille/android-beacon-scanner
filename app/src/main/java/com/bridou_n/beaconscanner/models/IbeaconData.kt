package com.bridou_n.beaconscanner.models

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by bridou_n on 10/09/2017.
 */

data class IbeaconData(
        @SerializedName("uuid")
        @ColumnInfo(name = "uuid")
        val uuid: String? = null,

        @SerializedName("major")
        @ColumnInfo(name = "major")
        val major: String? = null,

        @SerializedName("minor")
        @ColumnInfo(name = "minor")
        val minor: String? = null
)