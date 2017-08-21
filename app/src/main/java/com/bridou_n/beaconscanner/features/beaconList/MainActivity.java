package com.bridou_n.beaconscanner.features.beaconList;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bridou_n.beaconscanner.AppSingleton;
import com.bridou_n.beaconscanner.R;
import com.bridou_n.beaconscanner.events.Events;
import com.bridou_n.beaconscanner.events.RxBus;
import com.bridou_n.beaconscanner.features.settings.SettingsActivity;
import com.bridou_n.beaconscanner.models.BeaconSaved;
import com.bridou_n.beaconscanner.utils.BluetoothManager;
import com.bridou_n.beaconscanner.utils.DividerItemDecoration;
import com.bridou_n.beaconscanner.utils.PreferencesHelper;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.analytics.FirebaseAnalytics;

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
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, EasyPermissions.PermissionCallbacks {
    protected static final String TAG = "MAIN_ACTIVITY";
    private static final String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int RC_COARSE_LOCATION = 1;
    private static final int RC_SETTINGS_SCREEN = 2;

    private Disposable bluetoothStateDisposable;
    private Disposable rangeDisposable;
    private MaterialDialog dialog;
    private RealmResults<BeaconSaved> beaconResults;
    private boolean hasStartedTutorial = false;

    @Inject BluetoothManager bluetooth;
    @Inject BeaconManager beaconManager;
    @Inject RxBus rxBus;
    @Inject Realm realm;
    @Inject PreferencesHelper prefs;
    @Inject FirebaseAnalytics tracker;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.activity_main) CoordinatorLayout rootView;
    @BindView(R.id.bluetooth_state) TextView bluetoothState;

    @BindView(R.id.empty_view) RelativeLayout emptyView;
    @BindView(R.id.beacons_rv) RecyclerView beaconsRv;

    @BindView(R.id.scan_fab) FloatingActionButton scanFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppSingleton.Companion.getActivityComponent().inject(this);

        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                android.graphics.PorterDuff.Mode.MULTIPLY);

        beaconResults = realm.where(BeaconSaved.class).findAllSortedAsync(new String[]{"lastMinuteSeen", "distance"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING});

        beaconsRv.setHasFixedSize(true);
        beaconsRv.setLayoutManager(new LinearLayoutManager(this));
        beaconsRv.addItemDecoration(new DividerItemDecoration(this, null));
        beaconsRv.setAdapter(new BeaconsRecyclerViewAdapter(this, beaconResults, true));

        // Setup an observable on the bluetooth changes
        bluetoothStateDisposable = bluetooth.asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(e -> {
                    if (e instanceof Events.BluetoothState) {
                        bluetoothStateChanged(((Events.BluetoothState) e).getState());
                    }
                });
    }

    public void showTutorial() {
        TapTargetView.showFor(this,
                TapTarget.forToolbarMenuItem(toolbar, R.id.action_bluetooth, getString(R.string.bluetooth_control), getString(R.string.feature_bluetooth_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        bluetooth.enable();
                        TapTargetView.showFor(MainActivity.this,
                                TapTarget.forView(scanFab, getString(R.string.feature_scan_title), getString(R.string.feature_scan_content)).tintTarget(false).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        startScan();  // We start scanning for beacons
                                        TapTargetView.showFor(MainActivity.this,
                                                TapTarget.forToolbarMenuItem(toolbar, R.id.action_clear, getString(R.string.feature_clear_title), getString(R.string.feature_clear_content)).cancelable(false).dimColor(R.color.primaryText).drawShadow(true),
                                                new TapTargetView.Listener() {
                                                    @Override
                                                    public void onTargetClick(TapTargetView view) {
                                                        prefs.setHasSeenTutorial(true);
                                                        hasStartedTutorial = false;
                                                        super.onTargetClick(view);
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onResume() {
        beaconResults.addChangeListener(results -> {
            if (results.size() == 0 && emptyView.getVisibility() != View.VISIBLE) {
                beaconsRv.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else if (results.size() > 0 && beaconsRv.getVisibility() != View.VISIBLE) {
                beaconsRv.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        if (!prefs.hasSeenTutorial() && !hasStartedTutorial) {
            hasStartedTutorial = true;
            showTutorial();
        }

        // Start scanning if the scan on open is activated or if we were previously scanning
        if ((prefs.isScanOnOpen() && !isScanning()) || prefs.wasScanning()) {
            if (bluetooth.isEnabled()) {
                startScan();
            }
        }
        super.onResume();
    }

    private void updateUiWithBeaconsArround(Collection<Beacon> beacons) {
        realm.executeTransactionAsync(tRealm -> {
            Observable.fromIterable(beacons)
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

                        Bundle infos = new Bundle();

                        infos.putInt("manufacturer", beacon.getManufacturer());
                        infos.putInt("type", beacon.getBeaconType());
                        infos.putDouble("distance", beacon.getDistance());

                        tracker.logEvent("adding_or_updating_beacon", infos);
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
                stopScan();
                invalidateOptionsMenu();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                bluetoothState.setTextColor(ContextCompat.getColor(this, R.color.bluetoothTurningOffLight));
                bluetoothState.setBackgroundColor(ContextCompat.getColor(this, R.color.bluetoothTurningOff));
                bluetoothState.setText(getString(R.string.turning_bluetooth_off));
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

    public boolean bindBeaconManager() {
        if (EasyPermissions.hasPermissions(this, perms)) { // Ask permission and bind the beacon manager
            if (!beaconManager.isBound(this)) {
                beaconManager.bind(this);
            }
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, perms, RC_COARSE_LOCATION);
        }
        return false;
    }

    @OnClick(R.id.scan_fab)
    public void startStopScan() {
        if (!isScanning()) {
            tracker.logEvent("start_scanning_clicked", null);
            if (!bluetooth.isEnabled()) {
                Snackbar.make(rootView, getString(R.string.enable_bluetooth_to_start_scanning), Snackbar.LENGTH_LONG).show();
                return ;
            }
            startScan();
        } else {
            tracker.logEvent("stop_scanning_clicked", null);
            stopScan();
        }
    }

    public boolean isScanning() {
        return rangeDisposable != null && !rangeDisposable.isDisposed();
    }

    public void startScan() {
        if (!isScanning() && bindBeaconManager()) {
            rangeDisposable = rxBus.asFlowable() // Listen for range events
                    .observeOn(AndroidSchedulers.mainThread()) // We use this so we use the realm on the good thread & we can make UI changes
                    .subscribe(e -> {
                        if (e instanceof Events.RangeBeacon) {
                            updateUiWithBeaconsArround(((Events.RangeBeacon) e).getBeacons());
                        }
                    });

            toolbar.setTitle(getString(R.string.scanning_for_beacons));
            progress.setVisibility(View.VISIBLE);
            scanFab.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorPauseFab)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AnimatedVectorDrawable playToPause = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.play_to_pause_animation);

                scanFab.setImageDrawable(playToPause);
                playToPause.start();
            } else {
                scanFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause_icon));
            }
        }
    }

    public void stopScan() {
        if (isScanning()) {
            rangeDisposable.dispose(); // Stop listening for range events

            toolbar.setTitle(getString(R.string.app_name));
            progress.setVisibility(View.GONE);
            scanFab.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorAccent)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AnimatedVectorDrawable pauseToPlay = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.pause_to_play_animation);

                scanFab.setImageDrawable(pauseToPlay);
                pauseToPlay.start();
            } else {
                scanFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play_icon));
            }
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier((beacons, region) -> {
            rxBus.send(new Events.RangeBeacon(beacons, region));
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.bridou_n.beaconscanner", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        tracker.logEvent("permission_granted", null);
        startScan();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> permList) {
        if (requestCode == RC_COARSE_LOCATION) {
            tracker.logEvent("permission_denied", null);
            if (EasyPermissions.somePermissionPermanentlyDenied(this, permList)) {
                tracker.logEvent("permission_denied_permanently", null);
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
            menu.getItem(1).setIcon(R.drawable.ic_bluetooth_disabled_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                bluetooth.toggle();
                tracker.logEvent("action_bluetooth", null);
                break ;
            case R.id.action_clear:
                tracker.logEvent("action_clear", null);
                dialog = new MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title(R.string.delete_all)
                        .content(R.string.are_you_sure_delete_all)
                        .autoDismiss(true)
                        .onPositive((dialog, which) -> {
                            tracker.logEvent("action_clear_accepted", null);
                            realm.executeTransactionAsync(tRealm -> {
                                tRealm.where(BeaconSaved.class).findAll().deleteAllFromRealm();
                            });
                        })
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .build();
                dialog.show();
                break ;
            case R.id.action_settings:
                tracker.logEvent("action_settings", null);
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    protected void onPause() {
        prefs.setScanningState(isScanning());
        stopScan();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (beaconManager.isBound(this)) {
            // Only do this in onDestroy() not onPause()
            // Can't be bind() & unbind() several times
            beaconManager.unbind(this);
        }
        if (bluetoothStateDisposable != null && !bluetoothStateDisposable.isDisposed()) {
            bluetoothStateDisposable.dispose();
        }
        realm.close();
        super.onDestroy();
    }
}
