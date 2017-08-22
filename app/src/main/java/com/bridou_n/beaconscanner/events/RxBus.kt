package com.bridou_n.beaconscanner.events

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/**
 * Created by bridou_n on 05/10/2016.
 */

class RxBus {
    private val _bus = PublishRelay.create<Any>().toSerialized()

    fun send(o: Any) =  _bus.accept(o)

    fun asFlowable(): Flowable<Any>  = _bus.toFlowable(BackpressureStrategy.LATEST)

    fun hasObservers() =  _bus.hasObservers()
}