package com.bridou_n.beaconscanner.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.bridou_n.beaconscanner.R

/**
 * Created by bridou_n on 03/04/2017.
 */

class PreferencesHelper(ctx: Context) {
    
    companion object {
        private val SHARED_PREF_KEY = "shared_pref"

        private val TUTO_KEY = "tutoKey"
        private val SCANNING_STATE_KEY = "scanningState"
        private val SCAN_DELAY_KEY = "scanDelay"
        private val PREVENT_SLEEP_KEY = "preventSleep"
        private val LOGGING_ENABLED_KEY = "loggingEnabled"
        private val LOGGING_ENDPOINT_KEY = "loggingEndpoint"
        private val LOGGING_DEVICE_NAME_KEY = "loggingDeviceName"
        private val LOGGING_FREQUENCY = "loggingFrequency"
        private val LAST_LOGGING_CALL = "lastLoggingCall"
    }

    private val prefs: SharedPreferences
    private val ressources: Resources

    init {
        prefs = ctx.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        ressources = ctx.resources
    }

    var preventSleep: Boolean
        get() = prefs.getBoolean(PREVENT_SLEEP_KEY, false)
        set(status) = prefs.edit().putBoolean(PREVENT_SLEEP_KEY, status).apply()

    fun setHasSeenTutorial(status: Boolean) = prefs.edit().putBoolean(TUTO_KEY, status).apply()

    fun hasSeenTutorial() = prefs.getBoolean(TUTO_KEY, false)

    fun setScanningState(state: Boolean) = prefs.edit().putBoolean(SCANNING_STATE_KEY, state).apply()

    fun wasScanning() = prefs.getBoolean(SCANNING_STATE_KEY, false)

    fun setScanDelayIdx(delayIdx: Int) = prefs.edit().putInt(SCAN_DELAY_KEY, delayIdx).apply()

    fun getScanDelayIdx() = prefs.getInt(SCAN_DELAY_KEY, 0)

    fun getScanDelay() : Long {
        val idx = getScanDelayIdx()
        val scansDelays = ressources.getIntArray(R.array.scan_delays)

        if (idx < scansDelays.size) {
            return scansDelays[idx].toLong()
        }
        return 0L
    }

    fun getScanDelayName() : String {
        val idx = getScanDelayIdx()
        val scansDelays = ressources.getStringArray(R.array.scan_delays_names)

        if (idx < scansDelays.size) {
            return scansDelays[idx]
        }
        return scansDelays[0]
    }

    var isLoggingEnabled: Boolean
        get() = prefs.getBoolean(LOGGING_ENABLED_KEY, false)
        set(status) = prefs.edit().putBoolean(LOGGING_ENABLED_KEY, status).apply()

    var loggingEndpoint: String?
        get() = prefs.getString(LOGGING_ENDPOINT_KEY, null)
        set(endpoint) = prefs.edit().putString(LOGGING_ENDPOINT_KEY, endpoint).apply()

    var loggingDeviceName: String?
        get() = prefs.getString(LOGGING_DEVICE_NAME_KEY, null)
        set(name) = prefs.edit().putString(LOGGING_DEVICE_NAME_KEY, name).apply()

    fun setLoggingFrequencyIdx(delayIdx: Int) = prefs.edit().putInt(LOGGING_FREQUENCY, delayIdx).apply()

    fun getLoggingFrequencyIdx() = prefs.getInt(LOGGING_FREQUENCY, 0)

    /**
     * Get the number of scans to do before firing a logging request
     */
    fun getLoggingFrequency() : Int {
        val idx = getLoggingFrequencyIdx()
        val scansDelays = ressources.getIntArray(R.array.logging_frequencies)

        if (idx < scansDelays.size) {
            return scansDelays[idx]
        }
        return 0
    }

    fun getLoggingFrequencyName() : String {
        val idx = getLoggingFrequencyIdx()
        val loggingFrequencies = ressources.getStringArray(R.array.logging_frequencies_names)

        if (idx < loggingFrequencies.size) {
            return loggingFrequencies[idx]
        }
        return loggingFrequencies[0]
    }

    var lasLoggingCall: Long
        get() = prefs.getLong(LAST_LOGGING_CALL, 0)
        set(timestamp) = prefs.edit().putLong(LAST_LOGGING_CALL, timestamp).apply()

}
