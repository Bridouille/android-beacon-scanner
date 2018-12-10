package com.bridou_n.beaconscanner.utils

import com.bridou_n.beaconscanner.BuildConfig

object BuildTypes {

    fun isRelease() = BuildConfig.BUILD_TYPE.contentEquals("release")

    fun isDebug() = BuildConfig.BUILD_TYPE.contentEquals("debug")
}