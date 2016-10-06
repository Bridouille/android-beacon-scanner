# Android Beacon Scanner #

A simple android beacon scanner that can recognize iBeacons, Eddystone beacons (UID and URL, with or without TLM) and AltBeacons [available on Google Play](https://play.google.com/store/apps/details?id=com.bridou_n.beaconscanner).

![enter image description here](https://github.com/Bridouille/android-beacon-scanner/screenshots/Eddystone-UID+TLM.png)
![enter image description here](https://github.com/Bridouille/android-beacon-scanner/screenshots/Eddystone-URL+TLM.png)
![enter image description here](https://github.com/Bridouille/android-beacon-scanner/screenshots/scan_discovery.png)

Available for android 4.3+ and smartphones with Bluetooth LE.

## Features ##

The following informations are displayed:

 - The type of beacon (iBeacon, AltBeacon or Eddystone)
 - UUID, Major & Minor for iBeacons and AltBeacons
 - Manufacturer code
 - The RSSI and RX values
 - The approximation of the distance with the beacon (this is based on the RSSI value received and is NOT accurate)
 - NamespaceID & InstanceID for Eddystone-UID beacons
 - Clickable URL for Eddystone-URL beacons
 - TLM data sent with the Eddystone beacons (if any)
 - The last time the beacon has been seen

It can display several frames emitted by a single beacon, allowing you to see if a beacon emits multiple frames types (for example radius network beacons can emit iBeacon and AltBeacon at the same time)!

## Main libraries used ##

 - [AltBeacon](https://github.com/AltBeacon/android-beacon-library)
	 - Scanning for beacons nearby
	 - Making the difference between iBeacons, AltBeacons and Eddystone beacons
	 - Easily getting data emitted by the beacons (UUID, major, minor, namespaceID...)
 - [Realm](https://github.com/realm/realm-java)
	 - Storing and retrieving data on the phone
 - [RxJava (& RxAndroid)](https://github.com/ReactiveX/RxJava)
	 - Easily threading, low benefit in such a small app
 - [Dagger 2](https://google.github.io/dagger/)
	 - Inject dependencies such as Realm, BeaconManager and Animations into the app
 - [ButterKnife](https://github.com/JakeWharton/butterknife)
	 - Bind Android views to fields.
 - [TapTargetView](https://github.com/KeepSafe/TapTargetView)
	- Material design discovery feature made easy

If any of you guys have feedback about the utilisation I made of theses libraries in my project, I'd love to hear it!

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