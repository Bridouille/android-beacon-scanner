package com.bridou_n.beaconscanner.utils

import android.os.Build

object AndroidVersion {

    fun isLOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    fun isMOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun isNOrLater() =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    fun isNMR1OrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun isOreoOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}