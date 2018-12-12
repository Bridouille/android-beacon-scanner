package com.bridou_n.beaconscanner.utils

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bridou_n.beaconscanner.features.beaconList.BeaconListActivity
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import javax.inject.Inject

class BluetoothManager @Inject constructor(private val adapter: BluetoothAdapter?, context: Context) {

    private val subject: BehaviorProcessor<BeaconListActivity.BluetoothState> =
            BehaviorProcessor.createDefault<BeaconListActivity.BluetoothState>(getStaeFromAdapterState(adapter?.state ?: BluetoothAdapter.STATE_OFF))

    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                    val state = getStaeFromAdapterState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))

                    subject.onNext(state)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun getStaeFromAdapterState(state: Int) : BeaconListActivity.BluetoothState {
        return when (state) {
            BluetoothAdapter.STATE_OFF -> BeaconListActivity.BluetoothState.STATE_OFF
            BluetoothAdapter.STATE_TURNING_OFF -> BeaconListActivity.BluetoothState.STATE_TURNING_OFF
            BluetoothAdapter.STATE_ON -> BeaconListActivity.BluetoothState.STATE_ON
            BluetoothAdapter.STATE_TURNING_ON -> BeaconListActivity.BluetoothState.STATE_TURNING_ON
            else -> BeaconListActivity.BluetoothState.STATE_OFF
        }
    }

    fun disable() = adapter?.disable()

    fun enable() = adapter?.enable()

    fun asFlowable(): Flowable<BeaconListActivity.BluetoothState> {
        return subject
    }

    fun isEnabled() = adapter?.isEnabled == true

    fun toggle() {
        if (isEnabled()) {
            disable()
        } else {
            enable()
        }
    }
}
