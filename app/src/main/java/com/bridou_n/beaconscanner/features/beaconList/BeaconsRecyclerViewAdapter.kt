package com.bridou_n.beaconscanner.features.beaconList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.models.BeaconSaved.Companion.TYPE_ALTBEACON
import com.bridou_n.beaconscanner.models.BeaconSaved.Companion.TYPE_EDDYSTONE_UID
import com.bridou_n.beaconscanner.models.BeaconSaved.Companion.TYPE_EDDYSTONE_URL
import com.bridou_n.beaconscanner.models.BeaconSaved.Companion.TYPE_RUUVITAG
import com.bridou_n.beaconscanner.utils.extensionFunctions.toCoolFormat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.item_beacon.view.*
import java.util.*

/**
 * Created by bridou_n on 30/09/2016.
 */

typealias OnControlsOpen = (beacon: BeaconSaved) -> Unit

class BeaconsRecyclerViewAdapter(
        val ctx: Context,
        val clickListener: OnControlsOpen?
) : ListAdapter<BeaconSaved, BeaconsRecyclerViewAdapter.BeaconViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BeaconSaved>() {
            override fun areItemsTheSame(oldItem: BeaconSaved, newItem: BeaconSaved) = oldItem.hashcode == newItem.hashcode
            override fun areContentsTheSame(oldItem: BeaconSaved, newItem: BeaconSaved) = oldItem == newItem
        }
    }

    class BeaconViewHolder(itemView: View, val listener: OnControlsOpen?) : RecyclerView.ViewHolder(itemView) {

        private val beaconInfosAdapter = BeaconInfosRvAdapter()

        fun bindView(beacon: BeaconSaved) {
            val ctx = itemView.context
            
            itemView.header_container.setOnClickListener {
                true.also { listener?.invoke(beacon) }
            }

            itemView.beacon_type.text = ctx.getString(when (beacon.beaconType) {
                TYPE_ALTBEACON -> R.string.altbeacon
                TYPE_EDDYSTONE_UID -> R.string.eddystone_uid
                TYPE_EDDYSTONE_URL -> R.string.eddystone_url
                TYPE_RUUVITAG -> R.string.ruuvitag
                else -> R.string.ibeacon
            })
            itemView.distance.text = String.format(Locale.getDefault(), "%.2f", beacon.distance)

            itemView.address.text = beacon.beaconAddress
            itemView.manufacturer.text = String.format(Locale.getDefault(), "0x%04X", beacon.manufacturer)
            itemView.last_seen.text = DateUtils.getRelativeTimeSpanString(
                    beacon.lastSeen, Date().time,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()

            val beaconInfos = mutableListOf<BeaconInfo>().apply {
                // Adding iBeacon or AltBeacon data
                beacon.ibeaconData?.let {
                    add(BeaconInfo(ctx.getString(R.string.uuid), it.uuid))
                    add(BeaconInfo(ctx.getString(R.string.major), it.major))
                    add(BeaconInfo(ctx.getString(R.string.minor), it.minor))
                }

                add(BeaconInfo(ctx.getString(R.string.RSSI_title), String.format(ctx.getString(R.string.x_dbm), beacon.rssi)))
                add(BeaconInfo(ctx.getString(R.string.TX_title), String.format(ctx.getString(R.string.x_dbm), beacon.txPower)))

                // Adding Eddystone UID data
                beacon.eddystoneUidData?.let {
                    add(BeaconInfo(ctx.getString(R.string.namespace_id_title), it.namespaceId))
                    add(BeaconInfo(ctx.getString(R.string.instance_id_title), it.instanceId))
                }

                // Adding Eddystone URL data
                beacon.eddystoneUrlData?.let {
                    add(BeaconInfo(
                            title = ctx.getString(R.string.url_title),
                            content = it.url ?: "UNKNOWN",
                            onItemClicked = { onUrlClicked(it.url) }
                    ))
                }

                // Adding RuuviTag data
                beacon.ruuviData?.let {
                    add(BeaconInfo(ctx.getString(R.string.air_pressure_title), String.format(ctx.getString(R.string.x_hpa), it.airPressure)))
                    add(BeaconInfo(ctx.getString(R.string.temperature_title), ctx.getString(R.string.x_degrees, "${it.temperatue}")))
                    add(BeaconInfo(ctx.getString(R.string.humidity_title), String.format("%d %%", it.humidity)))
                }

                // Adding TLM data
                beacon.telemetryData?.let {
                    add(BeaconInfo(ctx.getString(R.string.battery_title), String.format(ctx.getString(R.string.x_mv), it.batteryMilliVolts)))
                    add(BeaconInfo(ctx.getString(R.string.temperature_title), ctx.getString(R.string.x_degrees, String.format("%.1f", it.temperature)))) // %.1f

                    add(BeaconInfo(ctx.getString(R.string.uptime_title), String.format(ctx.getString(R.string.x_seconds), it.uptime.toCoolFormat())))
                    add(BeaconInfo(ctx.getString(R.string.pdu_title), it.pduCount.toCoolFormat()))
                }
            }

            itemView.infos_rv.apply {
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.SPACE_BETWEEN
                }
                adapter = beaconInfosAdapter
                isNestedScrollingEnabled = false
            }
            beaconInfosAdapter.submitList(beaconInfos)
        }

        fun onUrlClicked(url: String?) {
            url ?: return

            try {
                val uri = Uri.parse(url)
                itemView.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) { }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_beacon, parent, false)

        return BeaconViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }
}
