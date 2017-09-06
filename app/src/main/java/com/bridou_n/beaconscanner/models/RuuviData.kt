package com.bridou_n.beaconscanner.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by bridou_n on 06/09/2017.
 */

open class RuuviData() : RealmObject() {

    /**
     * The air humidity in % age
     */
    @SerializedName("humidity") var humidity: Int = 0

    /**
     * The airPressure in hPa
     */
    @SerializedName("airPressure") var airPressure: Int = 0

    /**
     * The temperature in C°
     */
    @SerializedName("temperature")  var temperatue: Int = 0

    constructor(hum: Int, airP: Int, tmp: Int) : this() {
        humidity = hum
        airPressure = airP
        temperatue = tmp
    }

    fun clone() : RuuviData {
        var ret = RuuviData()

        ret.humidity = humidity
        ret.airPressure = airPressure
        ret.temperatue = temperatue

        return ret
    }
}