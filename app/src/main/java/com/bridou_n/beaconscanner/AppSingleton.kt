package com.bridou_n.beaconscanner

import android.util.Log.ERROR
import androidx.multidex.MultiDexApplication
import com.bridou_n.beaconscanner.dagger.AppComponent
import com.bridou_n.beaconscanner.dagger.ContextModule
import com.bridou_n.beaconscanner.dagger.DaggerAppComponent
import com.bridou_n.beaconscanner.utils.BuildTypes
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by bridou_n on 30/09/2016.
 */

class AppSingleton : MultiDexApplication() {
	
	companion object {
		lateinit var appComponent: AppComponent
	}
	
	@Inject
	lateinit var tracker: FirebaseAnalytics
	
	override fun onCreate() {
		super.onCreate()
		
		// Dagger
		appComponent = DaggerAppComponent.builder()
			.contextModule(ContextModule(this))
			.build()
		appComponent.inject(this)
		
		// Timber
		Timber.plant(CrashReportingTree())
		
		// Analytics
		tracker.setAnalyticsCollectionEnabled(BuildTypes.isRelease())
	}
}

/** A tree which logs important information for crash reporting.  */
class CrashReportingTree : Timber.DebugTree() {
	
	override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
		Crashlytics.log(priority, tag, message)
		
		t?.let {
			if (priority == ERROR) {
				Crashlytics.logException(t)
			}
		}
	}
}