package com.bridou_n.beaconscanner.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by bridou_n on 03/04/2017.
 */

class PreferencesHelper(ctx: Context) {
    companion object {
        private val SHARED_PREF_KEY = "shared_pref"

        private val TUTO_KEY = "tutoKey"
        private val SCAN_ON_OPEN_KEY = "scanOnOpenKey"
        private val SCANNING_STATE = "scanningState"
    }

    private val prefs: SharedPreferences

    init {
        prefs = ctx.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
    }

    var isScanOnOpen: Boolean
        get() = prefs.getBoolean(SCAN_ON_OPEN_KEY, false)
        set(status) = prefs.edit().putBoolean(SCAN_ON_OPEN_KEY, status).apply()

    fun setHasSeenTutorial(status: Boolean) = prefs.edit().putBoolean(TUTO_KEY, status).apply()

    fun hasSeenTutorial() = prefs.getBoolean(TUTO_KEY, false)

    fun setScanningState(state: Boolean) = prefs.edit().putBoolean(SCANNING_STATE, state).apply()

    fun wasScanning() = prefs.getBoolean(SCANNING_STATE, false)
}
