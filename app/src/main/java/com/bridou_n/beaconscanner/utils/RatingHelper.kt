package com.bridou_n.beaconscanner.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Created by bridou_n on 27/08/2017.
 */

class RatingHelper(ctx: Context) {

    companion object {
        private val SHARED_PREF_RATING = "shared_pref_rating"
        // We show the rating every 10 opennings until they rate
        private val APP_OPENS_STEPS = 10L

        private val APP_OPENS_KEY = "appOpenKey"
        private val POPUP_SEEN = "popupSeenKey"

        const val STEP_ONE = 1
        const val STEP_TWO = 2
    }

    private val prefs: SharedPreferences
    private var isRatingOngoing = false

    init {
        prefs = ctx.getSharedPreferences(SHARED_PREF_RATING, Context.MODE_PRIVATE)
    }

    fun getAppOpens() = prefs.getLong(APP_OPENS_KEY, 0)

    fun incrementAppOpens() = prefs.edit().putLong(APP_OPENS_KEY, getAppOpens() + 1).apply()

    fun hasSeenPopup() = prefs.getBoolean(POPUP_SEEN, false)

    fun setPopupSeen() = prefs.edit().putBoolean(POPUP_SEEN, true).apply()

    fun setRatingOngoing() {
        isRatingOngoing = true
    }

    fun shouldShowRatingRationale() = !isRatingOngoing && !hasSeenPopup() && getAppOpens() % APP_OPENS_STEPS == 0L

    fun reset() = prefs.edit().clear().apply()
}