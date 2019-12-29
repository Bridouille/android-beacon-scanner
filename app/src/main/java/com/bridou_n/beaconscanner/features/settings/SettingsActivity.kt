package com.bridou_n.beaconscanner.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.bridou_n.beaconscanner.BuildConfig
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.blockedList.BlockedActivity
import com.bridou_n.beaconscanner.utils.PreferencesHelper
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.bridou_n.beaconscanner.utils.extensionFunctions.log
import com.bridou_n.beaconscanner.utils.extensionFunctions.setHomeIcon
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() {
	
	@Inject lateinit var prefs: PreferencesHelper
	@Inject lateinit var tracker: FirebaseAnalytics
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		component().inject(this)
		
		setSupportActionBar(toolbar)
		supportActionBar?.title = ""
		supportActionBar?.setHomeIcon(R.drawable.ic_round_arrow_back_24px, R.color.colorOnBackground)
		
		scan_delay.text = prefs.getScanDelayName()
		prevent_sleep.isChecked = prefs.preventSleep
		logging_enabled.isChecked = prefs.isLoggingEnabled
		logging_endpoint.text = prefs.loggingEndpoint ?: getString(R.string.not_defined)
		device_name.text = prefs.loggingDeviceName ?: getString(R.string.not_defined)
		logging_frequency.text = prefs.getLoggingFrequencyName()
		
		handleLoggingState(prefs.isLoggingEnabled)
		
		toolbar_title.text = getString(R.string.settings)
		app_version.text = String.format("v%s", BuildConfig.VERSION_NAME)
		
		content.apply {
			viewTreeObserver.addOnScrollChangedListener {
				toolbar.isSelected = content.canScrollVertically(-1)
			}
		}
		
		scan_delay_container.setOnClickListener {
			MaterialDialog(this)
				.title(R.string.delay_in_between_each_scan)
				.listItemsSingleChoice(R.array.scan_delays_names,
					initialSelection = prefs.getScanDelayIdx(),
					waitForPositiveButton = true) { _, index, text ->
					Timber.d("$index - $text")
					
					prefs.setScanDelayIdx(index)
					scan_delay.text = prefs.getScanDelayName()
				}
				.positiveButton { }
				.show()
		}
		
		prevent_sleep_container.setOnClickListener { prevent_sleep.isChecked = !prevent_sleep.isChecked }
		prevent_sleep.setOnCheckedChangeListener { _, isChecked ->
			
			tracker.logEvent("prevent_sleep_changed", Bundle().apply {
				putBoolean("status", isChecked)
			})
			prefs.preventSleep = isChecked
		}
		
		logging_container.setOnClickListener { logging_enabled.isChecked = !logging_enabled.isChecked }
		logging_enabled.setOnCheckedChangeListener { _, isChecked ->
			tracker.logEvent("logging_changed", Bundle().apply {
				putBoolean("status", isChecked)
			})
			prefs.isLoggingEnabled = isChecked
			handleLoggingState(isChecked)
		}
		
		logging_frequency_container.setOnClickListener {
			MaterialDialog(this)
				.title(R.string.logging_frequency)
				.listItemsSingleChoice(R.array.logging_frequencies_names,
					initialSelection = prefs.getLoggingFrequencyIdx(),
					waitForPositiveButton = true) { _, idx, text ->
					Timber.d("$idx - $text")
					prefs.setLoggingFrequencyIdx(idx)
					logging_frequency.text = prefs.getLoggingFrequencyName()
				}
				.positiveButton { }
				.show()
		}
		
		device_name_container.setOnClickListener {
			MaterialDialog(this)
				.title(R.string.device_name)
				.input(
					hint = getString(R.string.device_name),
					prefill = prefs.loggingDeviceName ?: "Scanner 1",
					maxLength = 30,
					inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
					waitForPositiveButton = true
				) { _, input ->
					Timber.d("$input")
					
					val name = input.toString()
					if (name.isNotEmpty()) {
						device_name.text = name
						prefs.loggingDeviceName = name
					}
				}
				.negativeButton(android.R.string.cancel)
				.show()
		}
		
		logging_endpoint_container.setOnClickListener {
			MaterialDialog(this)
				.title(R.string.logging_endpoint)
				.input(
					hint = getString(R.string.logging_endpoint),
					prefill = prefs.loggingEndpoint ?: "http://example.com/logging",
					inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
					waitForPositiveButton = false
				) { dialog, input ->
					var endpoint = input.toString()
					
					if (endpoint.isNotEmpty()) {
						// If we just entered the IP address or 'example.com' for example
						if (!endpoint.startsWith("http")) {
							endpoint = "http://$endpoint"
						}
						
						Timber.d("endpoint: $endpoint - valid : ${Patterns.WEB_URL.matcher(endpoint).matches()}")
						
						if (Patterns.WEB_URL.matcher(endpoint).matches()) { // The URL is a valid endpoint
							dialog.getActionButton(WhichButton.POSITIVE).isEnabled = true
							return@input
						}
					}
					dialog.getActionButton(WhichButton.POSITIVE).isEnabled = false
				}
				.positiveButton {
					// From here the input should be valid
					var newEndpoint = it.getInputField()?.text?.toString() ?: return@positiveButton
					
					if (newEndpoint.isNotEmpty()) {
						// If we just entered the IP address or 'example.com' for example
						if (!newEndpoint.startsWith("http")) {
							newEndpoint = "http://$newEndpoint"
						}
						
						Timber.d("newEndpoint: $newEndpoint")
						
						logging_endpoint.text = newEndpoint
						prefs.loggingEndpoint = newEndpoint
					}
				}
				.negativeButton(android.R.string.cancel)
				.show()
		}
		
		blacklist.setOnClickListener {
			tracker.log("blacklist_clicked")
			
			startActivity(Intent(this, BlockedActivity::class.java))
		}
		
		rate.setOnClickListener {
			tracker.log("rate_clicked")
			val appPackageName = packageName
			
			try {
				startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
			} catch (anfe: android.content.ActivityNotFoundException) {
				startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
			}
		}
		
		open_source.setOnClickListener {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Bridouille/android-beacon-scanner")))
		}
		
		tutorial.setOnClickListener {
			tracker.log("tutorial_reset_clicked")
			prefs.setHasSeenTutorial(false)
			Snackbar.make(content, getString(R.string.the_tutorial_has_been_reset), Snackbar.LENGTH_LONG).show()
		}
	}
	
	fun handleLoggingState(isLoggingEnabled: Boolean) {
		listOf(
			logging_endpoint_container,
			device_name_container,
			logging_frequency_container,
			logging_div1, logging_div2, logging_div3
		).forEach {
			it.visibility = if (isLoggingEnabled) View.VISIBLE else View.GONE
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
