
# :cyclone: Android Beacon Scanner #

A simple android beacon scanner that can recognize iBeacons, AltBeacons, Eddystone beacons (UID and URL, with or without TLM) and RuuviTags [available on Google Play](https://play.google.com/store/apps/details?id=com.bridou_n.beaconscanner).

<img src="screenshots/screen-1.png" width="25%" />     <img src="screenshots/screen-2.png" width="25%" />   <img src="screenshots/screen-3.png" width="25%" />

Available for android 4.3+ and smartphones with Bluetooth LE.

## :key: Features ##

This app will scan for beacons near you! :v:

For all Beacons:
* The type of beacon (iBeacon, AltBeacon, Eddystone or RuuviTag)
* The approximation of the distance with the beacon (this is based on the RSSI value received and is NOT accurate)
* The beacon address and manufacturer code
* The RSSI and TX values
* The last time the beacon has been seen

For iBeacons and AltBeacons:
* UUID
* Major
* Minor

For Eddystone-UID beacons:
* NamespaceID
* InstanceID
* TLM data sent if any

For Eddystone-URL and RuuviTag
* Clickable URL
* TLM data sent if any

For RuuviTag:
* Air Pressure
* Temperature
* Humidity

It can display several frames emitted by a single beacon, allowing you to see if a beacon emits multiple frames types (for example radius network beacons can emit iBeacon and AltBeacon at the same time)!

* You can also log the scans of the beacons to an endpoint via a HTTP POST request.

## :wrench: Installation setup ##

 1. This project uses Firebase Analytics and Firebase Crash reporting, therefore it needs a `google-services.json` files in the `app` directory. To get it you need to create a Firebase project in the [Firebase console](https://console.firebase.google.com/u/0/) and go to the project settings and download the `google-services.json` file from there.
 2. You'll see there is a `signingConfigs.release` in the `app/build.gradle` that contains variable that you didn't declare `beacon_scanner_key_password`, `beacon_scanner_store_file`, `beacon_scanner_store_password`. Either you create those 3 keys in your `~/.gradle/gradle.properties` files or you can simply remove this configuration if you're not interested in having a release signing configuration. Make sure to remove it in the `release` and `preRelease` buildTypes as well.
 3. That's it, the app should now compile!

## :blue_book: Main libraries used ##

 - [AltBeacon](https://github.com/AltBeacon/android-beacon-library)
	 - Scanning for beacons nearby
	 - Making the difference between iBeacons, AltBeacons and Eddystone beacons
	 - Easily getting data emitted by the beacons (UUID, major, minor, namespaceID...)
 - [Realm & Realm adapters](https://github.com/realm/realm-java)
	 - Storing and retrieving data on the phone
 - [Retrofit](http://square.github.io/retrofit/)
	 - Network calls for POSTing to an endpoint
 - [RxJava (& RxAndroid)](https://github.com/ReactiveX/RxJava)
	 - Easily threading and network calls abstraction
 - [Dagger 2](https://google.github.io/dagger/)
	 - Inject dependencies such as Realm, BeaconManager, API services, Preferences into the app
 - [ButterKnife](https://github.com/JakeWharton/butterknife)
	 - Bind Android views to fields.
 - [TapTargetView](https://github.com/KeepSafe/TapTargetView)
	- Material design discovery feature made easy

## License ##

	Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

This software is available under the Apache License 2.0
