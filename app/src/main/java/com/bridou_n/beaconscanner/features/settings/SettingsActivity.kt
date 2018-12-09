package com.bridou_n.beaconscanner.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bridou_n.beaconscanner.BuildConfig
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.blockedList.BlockedActivity
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.RatingHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.bridou_n.beaconscanner.utils.extensionFunctions.logEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SettingsActivity"
    }

    @Inject lateinit var prefs: PreferencesHelper
    @Inject lateinit var ratingHelper: RatingHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        component().inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scan_open.isChecked = prefs.isScanOnOpen
        scan_delay.text = prefs.getScanDelayName()
        prevent_sleep.isChecked = prefs.preventSleep
        logging_enabled.isChecked = prefs.isLoggingEnabled
        logging_endpoint.text = prefs.loggingEndpoint ?: getString(R.string.not_defined)
        device_name.text = prefs.loggingDeviceName ?: getString(R.string.not_defined)
        logging_frequency.text = prefs.getLoggingFrequencyName()

        handleLoggingState(prefs.isLoggingEnabled)

        version.text = BuildConfig.VERSION_NAME

        scan_open.setOnCheckedChangeListener { _, isChecked ->

            tracker.logEvent("scan_open_changed", Bundle().apply {
                putBoolean("status", isChecked)
            })
            prefs.isScanOnOpen = isChecked
        }

        scan_delay_container.setOnClickListener {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title(R.string.delay_in_between_each_scan)
                    .items(R.array.scan_delays_names)
                    .itemsCallbackSingleChoice(prefs.getScanDelayIdx()) { _, _, which, text ->
                        Log.d(TAG, "$which - $text")
                        prefs.setScanDelayIdx(which)
                        scan_delay.text = prefs.getScanDelayName()
                        true
                    }
                    .positiveText(R.string.choose)
                    .show()
        }

        prevent_sleep.setOnCheckedChangeListener { _, isChecked ->

            tracker.logEvent("prevent_sleep_changed", Bundle().apply {
                putBoolean("status", isChecked)
            })
            prefs.preventSleep = isChecked
        }

        logging_enabled.setOnCheckedChangeListener { _, isChecked ->

            tracker.logEvent("logging_changed", Bundle().apply {
                putBoolean("status", isChecked)
            })
            prefs.isLoggingEnabled = isChecked
            handleLoggingState(isChecked)
        }


        logging_frequency_container.setOnClickListener {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title(R.string.logging_frequency)
                    .items(R.array.logging_frequencies_names)
                    .itemsCallbackSingleChoice(prefs.getLoggingFrequencyIdx()) { _, _, which, text ->
                        Log.d(TAG, "$which - $text")
                        prefs.setLoggingFrequencyIdx(which)
                        logging_frequency.text = prefs.getLoggingFrequencyName()
                        true
                    }
                    .positiveText(R.string.choose)
                    .show()
        }

        device_name_container.setOnClickListener {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title(R.string.device_name)
                    .inputRangeRes(2, 30, R.color.colorPauseFab)
                    .inputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
                    .input(getString(R.string.device_name), prefs.loggingDeviceName ?: "Scanner 1", { _, input ->
                        Log.d(TAG, "$input")

                        val name = input.toString()
                        if (name.isNotEmpty()) {
                            device_name.text = name
                            prefs.loggingDeviceName = name
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .show()
        }

        logging_endpoint_container.setOnClickListener {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title(R.string.logging_endpoint)
                    .inputRangeRes(7, -1, R.color.colorPauseFab)
                    .inputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
                    .input(getString(R.string.logging_endpoint), prefs.loggingEndpoint ?: "http://example.com/logging", { dialog, input ->
                        var endpoint = input.toString()

                        if (endpoint.isNotEmpty()) {
                            // If we just entered the IP address or 'example.com' for example
                            if (!endpoint.startsWith("http")) {
                                endpoint = "http://$endpoint"
                            }

                            Log.d(TAG, "endpoint: $endpoint - valid : " + Patterns.WEB_URL.matcher(endpoint).matches())

                            if (Patterns.WEB_URL.matcher(endpoint).matches()) { // The URL is a valid endpoint
                                dialog.getActionButton(DialogAction.POSITIVE).isEnabled = true
                                return@input
                            }
                        }
                        dialog.getActionButton(DialogAction.POSITIVE).isEnabled = false
                    })
                    .onPositive { dialog, _ ->
                        // From here the input should be valid
                        var newEndpoint = dialog.inputEditText?.text?.toString() ?: return@onPositive

                        if (newEndpoint.isNotEmpty()) {
                            // If we just entered the IP address or 'example.com' for example
                            if (!newEndpoint.startsWith("http")) {
                                newEndpoint = "http://$newEndpoint"
                            }

                            Log.d(TAG, "newEndpoint: " + newEndpoint)

                            logging_endpoint.text = newEndpoint
                            prefs.loggingEndpoint = newEndpoint
                        }
                    }
                    .negativeText(android.R.string.cancel)
                    .alwaysCallInputCallback()
                    .show()
        }

        blacklist.setOnClickListener {
            tracker.logEvent("blacklist_clicked")

            startActivity(Intent(this, BlockedActivity::class.java))
        }

        rate.setOnClickListener {
            tracker.logEvent("rate_clicked")
            ratingHelper.setPopupSeen()
            val appPackageName = packageName // getPackageName() from Context or Activity object

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
            }
        }

        feature_request.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Bridouille/android-beacon-scanner")))
        }

        tutorial.setOnClickListener {
            tracker.logEvent("tutorial_reset_clicked")
            prefs.setHasSeenTutorial(false)
            Snackbar.make(content, getString(R.string.the_tutorial_has_been_reset), Snackbar.LENGTH_LONG).show()
        }
    }

    fun handleLoggingState(isLoggingEnabled: Boolean) {
        listOf(logging_endpoint_title, device_name_title, logging_frequency_title).forEach {
            it.setTextColor(ContextCompat.getColor(this, if (isLoggingEnabled) R.color.primaryText else R.color.primaryTextDisabled))
        }
        listOf(logging_endpoint, device_name, logging_frequency).forEach {
            it.setTextColor(ContextCompat.getColor(this, if (isLoggingEnabled) R.color.secondaryText else R.color.secondaryTextDisabled ))
        }
        listOf(logging_endpoint_container, device_name_container, logging_frequency_container).forEach {
            it.isClickable = isLoggingEnabled
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
