package com.bridou_n.beaconscanner.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.view.MenuItem
import android.widget.ScrollView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick
import com.bridou_n.beaconscanner.BuildConfig
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    @Inject lateinit var prefs: PreferencesHelper
    @Inject lateinit var tracker: FirebaseAnalytics

    @BindView(R.id.content) lateinit var content: ScrollView
    @BindView(R.id.scan_open) lateinit var scanOpen: SwitchCompat
    @BindView(R.id.version) lateinit var version: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)
        component().inject(this)

        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanOpen.isChecked = prefs.isScanOnOpen
        version.text = BuildConfig.VERSION_NAME
    }

    @OnCheckedChanged(R.id.scan_open)
    fun onScanOpenChanged(status: Boolean) {
        val b = Bundle()

        b.putBoolean("status", status)
        tracker.logEvent("scan_open_changed", b)
        prefs.isScanOnOpen = status
    }

    @OnClick(R.id.rate)
    fun onRateClicked() {
        tracker.logEvent("rate_clicked", null)
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
        tracker.logEvent("tutorial_reset_clicked", null)
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
