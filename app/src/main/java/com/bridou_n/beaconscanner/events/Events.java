package com.bridou_n.beaconscanner.events;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Created by bridou_n on 05/10/2016.
 */

public class Events {
    public static class RangeBeacon {
        private Collection<Beacon> beacons;
        private Region region;

        public RangeBeacon(Collection<Beacon> beacons, Region region) {
            this.beacons = beacons;
            this.region = region;
        }

        public Collection<Beacon> getBeacons() {
            return beacons;
        }

        public void setBeacons(Collection<Beacon> beacons) {
            this.beacons = beacons;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }
    }

    public static class BluetoothState {
        private int state;

        public BluetoothState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
}
