package com.bridou_n.beaconscanner.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

import com.bridou_n.beaconscanner.events.Events;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class BluetoothManager {

    private final BluetoothAdapter adapter;

    private final BehaviorSubject<Object> subject;

    @Inject
    public BluetoothManager(@Nullable BluetoothAdapter adapter, Context context) {
        this.adapter = adapter;
        this.subject = BehaviorSubject.create(new Events.BluetoothState(adapter != null ? adapter.getState() : BluetoothAdapter.STATE_OFF));

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
