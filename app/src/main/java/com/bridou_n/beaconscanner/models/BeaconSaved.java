package com.bridou_n.beaconscanner.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by bridou_n on 30/09/2016.
 */

public class BeaconSaved extends RealmObject {
    @Ignore public static final int TYPE_EDDYSTONE_UID = 0;
    @Ignore public static final int TYPE_EDDYSTONE_URL = 1;
    @Ignore public static final int TYPE_ALTBEACON = 2;
    @Ignore public static final int TYPE_IBEACON = 3;

    @PrimaryKey
    private int hashcode; // hashcode()
    private int beaconType; // Eddystone, altBeacon, iBeacon
    private String beaconAddress; // MAC address of the bluetooth emitter
    private String UUID;
    private String Major;
    private String Minor;
    private int txPower;
    private int RSSI;
    private double distance;
    private Date lastSeen;
    private long lastMinuteSeen;
    private int manufacturer;
    private String URL;
    private String namespaceId;
    private String instanceId;
    private boolean hasTelemetryData;
    private long telemetryVersion;
    private long batteryMilliVolts;
    private long temperature;
    private long pduCount;
    private long uptime;

    public int getHashcode() {
        return hashcode;
    }

    public void setHashcode(int hashcode) {
        this.hashcode = hashcode;
    }

    public int getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(int beaconType) {
        this.beaconType = beaconType;
    }

    public String getBeaconAddress() {
        return beaconAddress;
    }

    public void setBeaconAddress(String beaconAddress) {
        this.beaconAddress = beaconAddress;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getMajor() {
        return Major;
    }

    public void setMajor(String major) {
        Major = major;
    }

    public String getMinor() {
        return Minor;
    }

    public void setMinor(String minor) {
        Minor = minor;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getLastMinuteSeen() {
        return lastMinuteSeen;
    }

    public void setLastMinuteSeen(long lastMinuteSeen) {
        this.lastMinuteSeen = lastMinuteSeen;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(int manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isHasTelemetryData() {
        return hasTelemetryData;
    }

    public void setHasTelemetryData(boolean hasTelemetryData) {
        this.hasTelemetryData = hasTelemetryData;
    }

    public double getTemperature() {
        double tmp = (float)temperature / 256.0;

        if (tmp == (float)(1 << 7)) { // 0x8000
            return 0;
        }
        return tmp > (float)(1 << 7) ? tmp - (float)(1 << 8) : tmp;
    }

    public void setTemperature(long temperature) {
        this.temperature = temperature;
    }

    public long getTelemetryVersion() {
        return telemetryVersion;
    }

    public void setTelemetryVersion(long telemetryVersion) {
        this.telemetryVersion = telemetryVersion;
    }

    public long getBatteryMilliVolts() {
        return batteryMilliVolts;
    }

    public void setBatteryMilliVolts(long batteryMilliVolts) {
        this.batteryMilliVolts = batteryMilliVolts;
    }

    public long getPduCount() {
        return pduCount;
    }

    public void setPduCount(long pduCount) {
        this.pduCount = pduCount;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }
}
