package com.bridou_n.beaconscanner.events

import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Region

/**
 * Created by bridou_n on 05/10/2016.
 */

class Events {
    class RangeBeacon(var beacons: Collection<Beacon>, var region: Region)

    class BluetoothState(var state: Int)
}
