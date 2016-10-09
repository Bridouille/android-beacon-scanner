package com.bridou_n.beaconscanner.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.bridou_n.beaconscanner.events.Events;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class BluetoothManager {

    private final BluetoothAdapter adapter;

    private final BehaviorSubject<Object> subject;

    @Inject
    public BluetoothManager(BluetoothAdapter adapter, Context context) {
        this.adapter = adapter;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    subject.onNext(new Events.BluetoothState(
                            intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR)));
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        // Get current state _after_ registering for broadcasts, just in case the state is changing
        this.subject = BehaviorSubject.create(new Events.BluetoothState(adapter.getState()));
    }

    public void disable() {
        if (adapter != null) {
            adapter.disable();
        }
    }

    public void enable() {
        if (adapter!= null) {
            adapter.enable();
        }
    }

    public boolean isEnabled() {
        return (adapter != null) && adapter.isEnabled();
    }

    public Observable<Object> observe() {
        return subject;
    }

    public void toggle() {
        if (isEnabled()) {
            disable();
        } else {
            enable();
        }
    }
}
