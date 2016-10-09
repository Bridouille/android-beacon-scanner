package com.bridou_n.beaconscanner.features.beaconList;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bridou_n.beaconscanner.AppSingleton;
import com.bridou_n.beaconscanner.R;
import com.bridou_n.beaconscanner.events.Events;
import com.bridou_n.beaconscanner.events.RxBus;
import com.bridou_n.beaconscanner.models.BeaconSaved;
import com.bridou_n.beaconscanner.utils.BluetoothManager;
import com.bridou_n.beaconscanner.utils.DividerItemDecoration;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, EasyPermissions.PermissionCallbacks {
    protected static final String TAG = "MAIN_ACTIVITY";
    private static final String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int RC_COARSE_LOCATION = 1;
    private static final int RC_SETTINGS_SCREEN = 2;
    private static final String PREF_TUTO_KEY = "PREF_TUTO_KEY";
    private static final String STATE_SCANNING = "scanState";

    private Subscription sub = null;
    private Subscription btSub = null;

    @Inject @Named("fab_search") Animation rotate;
    @Inject BluetoothManager bluetooth;
    @Inject BeaconManager beaconManager;
    @Inject RxBus rxBus;
    @Inject Realm realm;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main) CoordinatorLayout rootView;
    @BindView(R.id.bluetooth_state) TextView bluetoothState;

    @BindView(R.id.empty_view) RelativeLayout emptyView;
    @BindView(R.id.beacons_rv) RecyclerView beaconsRv;
    @BindView(R.id.scan_fab) FloatingActionButton scanFab;
    @BindView(R.id.scan_progress) ProgressBar scanProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppSingleton.activityComponent().inject(this);

        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main_menu);

        RealmResults<BeaconSaved> beaconResults = realm.where(BeaconSaved.class).findAllSortedAsync(new String[]{"lastMinuteSeen", "distance"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING});

        beaconResults.addChangeListener(results -> {
            if (results.size() == 0 && emptyView.getVisibility() != View.VISIBLE) {
                beaconsRv.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else if (results.size() > 0 && beaconsRv.getVisibility() != View.VISIBLE) {
                beaconsRv.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        beaconsRv.setHasFixedSize(true);
        beaconsRv.setLayoutManager(new LinearLayoutManager(this));
        beaconsRv.addItemDecoration(new DividerItemDecoration(this, null));
        beaconsRv.setAdapter(new BeaconsRecyclerViewAdapter(this, beaconResults, true));

        // Set our event handler
        sub = rxBus.toObserverable()
                .observeOn(AndroidSchedulers.mainThread()) // We use this so we use the realm on the good thread & we can make UI changes
                .subscribe(e -> {
                    if (e instanceof Events.RangeBeacon) {
                        updateUiWithBeaconsArround(((Events.RangeBeacon) e).getBeacons());
                    }
                });

        btSub = bluetooth.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e -> {
                    if (e instanceof Events.BluetoothState) {
                        bluetoothStateChanged(((Events.BluetoothState) e).getState());
                    }
                });

        if (!getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_TUTO_KEY, false)) {
            showTutorial();
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_SCANNING)) {
            startScan();
        }
    }

    public void showTutorial() {
        AppCompatActivity _this = this;

        TapTargetView.showFor(this,
                TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        bluetooth.enable();
                        TapTargetView.showFor(_this,
                                TapTarget.forView(scanFab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content)).tintTarget(false).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        startStopScan(); // We start scanning for beacons
                                        TapTargetView.showFor(_this,
                                                TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                                new TapTargetView.Listener() {
                                                    @Override
                                                    public void onTargetClick(TapTargetView view) {
                                                        super.onTargetClick(view);
                                                        getPreferences(Context.MODE_PRIVATE).edit().putBoolean(PREF_TUTO_KEY, true).apply();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateUiWithBeaconsArround(Collection<Beacon> beacons) {
        realm.executeTransactionAsync(tRealm -> {
            Observable.from(beacons)
                    .subscribe(b -> {
                        BeaconSaved beacon = new BeaconSaved();

                        // Common field to every beacon
                        beacon.setHashcode(b.hashCode());
                        beacon.setLastSeen(new Date());
                        beacon.setLastMinuteSeen(new Date().getTime() / 1000 / 60);
                        beacon.setBeaconAddress(b.getBluetoothAddress());
                        beacon.setRSSI(b.getRssi());
                        beacon.setManufacturer(b.getManufacturer());
                        beacon.setTxPower(b.getTxPower());
                        beacon.setDistance(b.getDistance());
                        if (b.getServiceUuid() == 0xfeaa) { // This is an Eddystone beacon
                            // Do we have telemetry data?
                            if (b.getExtraDataFields().size() > 0) {
                                beacon.setHasTelemetryData(true);
                                beacon.setTelemetryVersion(b.getExtraDataFields().get(0));
                                beacon.setBatteryMilliVolts(b.getExtraDataFields().get(1));
                                beacon.setTemperature(b.getExtraDataFields().get(2));
                                beacon.setPduCount(b.getExtraDataFields().get(3));
                                beacon.setUptime(b.getExtraDataFields().get(4));
                            } else {
                                beacon.setHasTelemetryData(false);
                            }

                            switch (b.getBeaconTypeCode()) {
                                case 0x00:
                                    beacon.setBeaconType(BeaconSaved.TYPE_EDDYSTONE_UID);
                                    // This is a Eddystone-UID frame
                                    beacon.setNamespaceId(b.getId1().toString());
                                    beacon.setInstanceId(b.getId2().toString());
                                    break;
                                case 0x10:
                                    beacon.setBeaconType(BeaconSaved.TYPE_EDDYSTONE_URL);
                                    // This is a Eddystone-URL frame
                                    beacon.setURL(UrlBeaconUrlCompressor.uncompress(b.getId1().toByteArray()));
                                    break;
                            }
                        } else { // This is an iBeacon or ALTBeacon
                            beacon.setBeaconType(b.getBeaconTypeCode() == 0xbeac? BeaconSaved.TYPE_ALTBEACON : BeaconSaved.TYPE_IBEACON); // 0x4c000215 is iBeacon
                            beacon.setUUID(b.getId1().toString());
                            beacon.setMajor(b.getId2().toString());
                            beacon.setMinor(b.getId3().toString());
                        }
                        tRealm.copyToRealmOrUpdate(beacon);
                    });
        });
    }

    private void bluetoothStateChanged(int state) {
        bluetoothState.setVisibility(View.VISIBLE);
         switch (state) {
            case BluetoothAdapter.STATE_OFF:
                bluetoothState.setTextColor(ContextCompat.getColor(this, R.color.bluetoothDisabledLight));
                bluetoothState.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothDisabled));
                bluetoothState.setText(getString(R.string.bluetooth_disabled));
                invalidateOptionsMenu();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                bluetoothState.setTextColor(ContextCompat.getColor(this, R.color.bluetoothTurningOffLight));
                bluetoothState.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothTurningOff));
                bluetoothState.setText(getString(R.string.turning_bluetooth_off));
                stopScan();
                break;
            case BluetoothAdapter.STATE_ON:
                bluetoothState.setVisibility(View.GONE); // If the bluetooth is ON, we don't warn the user
                bluetoothState.setText(getString(R.string.bluetooth_enabled));
                invalidateOptionsMenu();
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                bluetoothState.setTextColor(ContextCompat.getColor(this, R.color.bluetoothTurningOnLight));
                bluetoothState.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothTurningOn));
                bluetoothState.setText(getString(R.string.turning_bluetooth_on));
                break;
        }
    }

    public void bindBeaconManager() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            beaconManager.bind(this);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, perms, RC_COARSE_LOCATION);
        }
    }

    @OnClick(R.id.scan_fab)
    public void startStopScan() {
        if (!beaconManager.isBound(this)) {
            if (!bluetooth.isEnabled()) {
                Snackbar.make(rootView, getString(R.string.enable_bluetooth_to_start_scanning), Snackbar.LENGTH_LONG).show();
                return ;
            }
            startScan();
        } else {
            stopScan();
        }
    }

    public void startScan() {
        bindBeaconManager();
        rotate.setRepeatCount(Animation.INFINITE);
        scanFab.startAnimation(rotate);
        scanProgress.setVisibility(View.VISIBLE);
        toolbar.setTitle(getString(R.string.scanning_for_beacons));
    }

    public void stopScan() {
        beaconManager.unbind(this);
        rotate.setRepeatCount(0);
        scanProgress.setVisibility(View.INVISIBLE);
        toolbar.setTitle(getString(R.string.app_name));
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier((beacons, region) -> {
            rxBus.send(new Events.RangeBeacon(beacons, region));
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.bridou_n.beaconscanner", null, null, null));
        } catch (RemoteException e) {
            rxBus.sendError(e);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        bindBeaconManager();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> permList) {
        if (requestCode == RC_COARSE_LOCATION) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, permList)) {
                showPermissionSnackbar();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, perms, RC_COARSE_LOCATION);
            }
        }
    }

    public void showPermissionSnackbar() {
        final Snackbar snackBar = Snackbar.make(rootView, getString(R.string.enable_permission_from_settings), Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction(getString(R.string.enable),v -> {
            snackBar.dismiss();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivityForResult(intent, RC_SETTINGS_SCREEN);
        });
        snackBar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (!bluetooth.isEnabled()) {
            menu.getItem(1).setIcon(R.mipmap.ic_bluetooth_disabled_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_bluetooth) {
            bluetooth.toggle();
            return true;
        }
        if (id == R.id.action_clear) {
            realm.executeTransactionAsync(tRealm -> {
               tRealm.where(BeaconSaved.class).findAll().deleteAllFromRealm();
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_SCANNING, beaconManager.isBound(this)); // save the scanning state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sub != null && !sub.isUnsubscribed()) {
            sub.unsubscribe();
        }
        if (btSub != null && !btSub.isUnsubscribed()) {
            btSub.unsubscribe();
        }
        if (beaconManager.isBound(this)) {
            beaconManager.unbind(this);
        }
        realm.close();
    }
}
