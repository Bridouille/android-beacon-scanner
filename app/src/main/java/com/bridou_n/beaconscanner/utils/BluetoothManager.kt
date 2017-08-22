package com.bridou_n.beaconscanner.utils

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import com.bridou_n.beaconscanner.events.Events

import javax.inject.Inject

import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class BluetoothManager @Inject constructor(private val adapter: BluetoothAdapter?, context: Context) {

    private val subject: BehaviorProcessor<Any> = BehaviorProcessor.createDefault<Any>(Events.BluetoothState(adapter?.state ?: BluetoothAdapter.STATE_OFF))

    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                    subject.onNext(Events.BluetoothState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)))
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun disable() = adapter?.disable()

    fun enable() = adapter?.enable()

    fun asFlowable(): Flowable<Any> {
        return subject
    }

    val isEnabled: Boolean
        get() = adapter != null && adapter.isEnabled

    fun toggle() {
        if (isEnabled) {
            disable()
        } else {
            enable()
        }
    }
}
