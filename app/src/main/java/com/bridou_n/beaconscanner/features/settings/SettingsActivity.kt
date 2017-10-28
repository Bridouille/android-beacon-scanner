package com.bridou_n.beaconscanner.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import butterknife.*
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
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SettingsActivity"
    }

    @Inject lateinit var prefs: PreferencesHelper
    @Inject lateinit var ratingHelper: RatingHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.content) lateinit var content: ScrollView
    @BindView(R.id.scan_open) lateinit var scanOpen: SwitchCompat
    @BindView(R.id.scan_delay) lateinit var scanDelay: TextView
    @BindView(R.id.prevent_sleep) lateinit var preventSleep: SwitchCompat

    @BindView(R.id.logging_enabled) lateinit var loggingEnabled: SwitchCompat
    @BindView(R.id.logging_endpoint) lateinit var loggingEndpoint: TextView
    @BindView(R.id.device_name) lateinit var loggingDeviceName: TextView
    @BindView(R.id.logging_frequency) lateinit var loggingFrequency: TextView

    @BindView(R.id.version) lateinit var version: TextView

    @BindViews(R.id.logging_endpoint_title, R.id.device_name_title, R.id.logging_frequency_title)
    lateinit var loggingTitles: List<@JvmSuppressWildcards TextView>
    @BindViews(R.id.logging_endpoint, R.id.device_name, R.id.logging_frequency)
    lateinit var loggingSubtitles: List<@JvmSuppressWildcards TextView>
    @BindViews(R.id.logging_endpoint_container, R.id.device_name_container, R.id.logging_frequency_container)
    lateinit var loggingContainers: List<@JvmSuppressWildcards LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)
        component().inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanOpen.isChecked = prefs.isScanOnOpen
        scanDelay.text = prefs.getScanDelayName()
        preventSleep.isChecked = prefs.preventSleep
        loggingEnabled.isChecked = prefs.isLoggingEnabled
        loggingEndpoint.text = prefs.loggingEndpoint ?: getString(R.string.not_defined)
        loggingDeviceName.text = prefs.loggingDeviceName ?: getString(R.string.not_defined)
        loggingFrequency.text = prefs.getLoggingFrequencyName()

        handleLoggingState(prefs.isLoggingEnabled)

        version.text = BuildConfig.VERSION_NAME
    }

    fun handleLoggingState(isLoggingEnabled: Boolean) {
        loggingTitles.forEach {
            it.setTextColor(ContextCompat.getColor(this, if (isLoggingEnabled) R.color.primaryText else R.color.primaryTextDisabled))
        }
        loggingSubtitles.forEach {
            it.setTextColor(ContextCompat.getColor(this, if (isLoggingEnabled) R.color.secondaryText else R.color.secondaryTextDisabled ))
        }
        loggingContainers.forEach {
            it.isClickable = isLoggingEnabled
        }
    }

    @OnCheckedChanged(R.id.scan_open)
    fun onScanOpenChanged(status: Boolean) {
        val b = Bundle()

        b.putBoolean("status", status)
        tracker.logEvent("scan_open_changed", b)
        prefs.isScanOnOpen = status
    }

    @OnClick(R.id.scan_delay_container)
    fun onScanDelayClicked() {
        MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(R.string.delay_in_between_each_scan)
                .items(R.array.scan_delays_names)
                .itemsCallbackSingleChoice(prefs.getScanDelayIdx()) { _, _, which, text ->
                    Log.d(TAG, "$which - $text")
                    prefs.setScanDelayIdx(which)
                    scanDelay.text = prefs.getScanDelayName()
                    true
                }
                .positiveText(R.string.choose)
                .show()
    }

    @OnCheckedChanged(R.id.prevent_sleep)
    fun onPreventSleepChanged(status: Boolean) {
        val b = Bundle()

        b.putBoolean("status", status)
        tracker.logEvent("prevent_sleep_changed", b)
        prefs.preventSleep = status
    }

    @OnCheckedChanged(R.id.logging_enabled)
    fun onLoggingChanged(status: Boolean) {
        val b = Bundle()

        b.putBoolean("status", status)
        tracker.logEvent("logging_changed", b)
        prefs.isLoggingEnabled = status
        handleLoggingState(status)
    }

    @OnClick(R.id.logging_endpoint_container)
    fun onLoggingEndpointClicked() {
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

                        loggingEndpoint.text = newEndpoint
                        prefs.loggingEndpoint = newEndpoint
                    }
                }
                .negativeText(android.R.string.cancel)
                .alwaysCallInputCallback()
                .show()
    }

    @OnClick(R.id.device_name_container)
    fun onLoggingDeviceNameClicked() {
        MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(R.string.device_name)
                .inputRangeRes(2, 30, R.color.colorPauseFab)
                .inputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
                .input(getString(R.string.device_name), prefs.loggingDeviceName ?: "Scanner 1", { _, input ->
                    Log.d(TAG, "$input")

                    val name = input.toString()
                    if (name.isNotEmpty()) {
                        loggingDeviceName.text = name
                        prefs.loggingDeviceName = name
                    }
                })
                .negativeText(android.R.string.cancel)
                .show()
    }

    @OnClick(R.id.logging_frequency_container)
    fun onLoggingFrequencyClicked() {
        MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(R.string.logging_frequency)
                .items(R.array.logging_frequencies_names)
                .itemsCallbackSingleChoice(prefs.getLoggingFrequencyIdx()) { _, _, which, text ->
                    Log.d(TAG, "$which - $text")
                    prefs.setLoggingFrequencyIdx(which)
                    loggingFrequency.text = prefs.getLoggingFrequencyName()
                    true
                }
                .positiveText(R.string.choose)
                .show()
    }

    @OnClick(R.id.blacklist)
    fun onBlackListClicked() {
        tracker.logEvent("blacklist_clicked")

        startActivity(Intent(this, BlockedActivity::class.java))
    }

    @OnClick(R.id.rate)
    fun onRateClicked() {
        tracker.logEvent("rate_clicked")
        ratingHelper.setPopupSeen()
        val appPackageName = packageName // getPackageName() from Context or Activity object

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
        }
    }

    @OnClick(R.id.feature_request)
    fun onFeatureRequestClicked() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", BuildConfig.CONTACT_EMAIL, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feature request for BeaconScanner")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
    }

    @OnClick(R.id.tutorial)
    fun onTutorialClicked() {
        tracker.logEvent("tutorial_reset_clicked")
        prefs.setHasSeenTutorial(false)
        Snackbar.make(content, getString(R.string.the_tutorial_has_been_reset), Snackbar.LENGTH_LONG).show()
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
