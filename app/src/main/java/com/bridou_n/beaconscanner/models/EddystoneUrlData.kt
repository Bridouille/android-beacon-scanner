package com.bridou_n.beaconscanner.models

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Created by bridou_n on 10/09/2017.
 */

data class EddystoneUrlData(
        @SerializedName("url")
        @ColumnInfo(name = "url")
        var url: String? = null
)