package com.bridou_n.beaconscanner.features.beaconList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.CountHelper
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.beacon_item.view.*
import kotlinx.android.synthetic.main.eddystone_uid_item.view.*
import kotlinx.android.synthetic.main.eddystone_url_item.view.*
import kotlinx.android.synthetic.main.ibeacon_altbeacon_item.view.*
import kotlinx.android.synthetic.main.ruuvitag_item.view.*
import java.text.DateFormat
import java.util.*

/**
 * Created by bridou_n on 30/09/2016.
 */

class BeaconsRecyclerViewAdapter(val data: RealmResults<BeaconSaved>,
                                 val ctx: Context,
                                 val onLongClickListener: OnControlsOpen?) :
        RealmRecyclerViewAdapter<BeaconSaved, BeaconsRecyclerViewAdapter.BaseHolder>(data, true) {

    open class BaseHolder(itemView: View, val ctx: Context, val listener: OnControlsOpen?) : RecyclerView.ViewHolder(itemView) {

        protected var beaconDisplayed: BeaconSaved? = null

        open fun bindView(beacon: BeaconSaved) {
            beaconDisplayed = beacon

            itemView.visit_website_btn.setOnClickListener {
                onUrlClicked()
            }
            itemView.ruuvi_visit_website_btn.setOnClickListener {
                onUrlClicked()
            }
            itemView.card.setOnLongClickListener {
                beaconDisplayed?.let {
                    listener?.onOpenControls(it)
                }
                true
            }

            itemView.address.text = beacon.beaconAddress
            itemView.distance.text = String.format(Locale.getDefault(), "%.2f", beacon.distance)
            itemView.manufacturer.text = String.format(Locale.getDefault(), "0x%04X", beacon.manufacturer)
            itemView.last_seen.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault()).format(Date(beacon.lastSeen))

            itemView.rssi.text = String.format(ctx.getString(R.string.rssi_x_dbm), beacon.rssi)
            itemView.tx.text = String.format(ctx.getString(R.string.tx_x_dbm), beacon.txPower)

            val tlmData = listOf(itemView.battery_container,
                    itemView.temperature_container,
                    itemView.uptime_container,
                    itemView.pdu_sent_container)

            val telemetry = beacon.telemetryData
            if (telemetry != null) {
                tlmData.forEach { it.visibility = View.VISIBLE }
                itemView.battery.text = String.format(ctx.getString(R.string.battery_x_mv), telemetry.batteryMilliVolts)
                itemView.temperature.text = String.format(ctx.getString(R.string.temperature_x_degres), telemetry.temperature) // %.1f
                itemView.uptime.text = String.format(ctx.getString(R.string.uptime_x_seconds), CountHelper.coolFormat(telemetry.uptime.toDouble(), 0))
                itemView.pdu_sent.text = String.format(ctx.getString(R.string.x_packets_sent), CountHelper.coolFormat(telemetry.pduCount.toDouble(), 0))
            } else {
                tlmData.forEach { it.visibility = View.GONE }
            }
        }

        fun hideAllLayouts() {
            itemView.ibeacon_altbeacon_item.visibility = View.GONE
            itemView.eddystone_uid_item.visibility = View.GONE
            itemView.eddystone_url_item.visibility = View.GONE
            itemView.ruuvitag_item.visibility = View.GONE
        }

        fun onUrlClicked() {
            val url = beaconDisplayed?.eddystoneUrlData?.url
            try {
                val uri = Uri.parse(url)
                ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) { }
        }
    }

    class IBeaconAltBeaconHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            itemView.card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.ibeaconBackground))
            hideAllLayouts()
            itemView.ibeacon_altbeacon_item.visibility = View.VISIBLE

            itemView.beacon_type.text = String.format(Locale.getDefault(), "%s",
                    if (beacon.beaconType == BeaconSaved.TYPE_IBEACON) itemView.context.getString(R.string.ibeacon)
                    else itemView.context.getString(R.string.altbeacon))

            val ibeaconData = beacon.ibeaconData
            if (ibeaconData != null) {
                itemView.proximity_uuid.text = String.format(ctx.getString(R.string.uuid_x), ibeaconData.uuid)
                itemView.major.text = String.format(ctx.getString(R.string.major_x), ibeaconData.major)
                itemView.minor.text = String.format(ctx.getString(R.string.minor_x), ibeaconData.minor)
            }
        }
    }

    class EddystoneUidHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            itemView.card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.eddystoneUidBackground))
            hideAllLayouts()
            itemView.eddystone_uid_item.visibility = View.VISIBLE

            itemView.beacon_type.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_uid),
                    if (beacon.telemetryData != null) itemView.context.getString(R.string.plus_tlm) else "")
            val eddystoneUidData = beacon.eddystoneUidData
            if (eddystoneUidData != null) {
                itemView.namespace_id.text = String.format(ctx.getString(R.string.namespace_id_x), eddystoneUidData.namespaceId)
                itemView.instance_id.text = String.format(ctx.getString(R.string.instance_id_x), eddystoneUidData.instanceId)
            }
        }
    }

    class EddystoneUrlHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            itemView.card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.eddystoneUrlBackground))
            hideAllLayouts()
            itemView.eddystone_url_item.visibility = View.VISIBLE

            itemView.beacon_type.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_url),
                    if (beacon.telemetryData != null) itemView.context.getString(R.string.plus_tlm) else "")
            itemView.url.text = String.format(ctx.getString(R.string.url_x), beacon.eddystoneUrlData?.url)
        }
    }

    class RuuviTagHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            itemView.card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.ruuvitagBackground))
            hideAllLayouts()
            itemView.ruuvitag_item.visibility = View.VISIBLE

            itemView.beacon_type.text = String.format(Locale.getDefault(), "%s", itemView.context.getString(R.string.ruuvitag))
            itemView.ruuvi_url.text = String.format(ctx.getString(R.string.url_x), beacon.eddystoneUrlData?.url)

            val ruuviData = beacon.ruuviData
            if (ruuviData != null) {
                itemView.ruuvi_air_pressure.text = String.format(ctx.getString(R.string.air_pressure_x_hpa), ruuviData.airPressure)
                itemView.ruuvi_temperature.text = String.format(ctx.getString(R.string.temperature_d_degres), ruuviData.temperatue)
                itemView.ruuvi_humidity.text = String.format(ctx.getString(R.string.humidity_x_percent), ruuviData.humidity)
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= data.size) {
            return R.layout.footer_item
        }
        val b = getItem(position)

        when (b?.beaconType) {
            BeaconSaved.TYPE_EDDYSTONE_UID -> return R.layout.eddystone_uid_item
            BeaconSaved.TYPE_EDDYSTONE_URL -> return R.layout.eddystone_url_item
            BeaconSaved.TYPE_RUUVITAG -> return R.layout.ruuvitag_item
            BeaconSaved.TYPE_ALTBEACON, BeaconSaved.TYPE_IBEACON -> return R.layout.ibeacon_altbeacon_item
            else -> return R.layout.ibeacon_altbeacon_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        // The actual layout file to inflate
        val layout = if (viewType == R.layout.footer_item) viewType else R.layout.beacon_item
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        when (viewType) {
            R.layout.eddystone_uid_item -> return EddystoneUidHolder(view, ctx, onLongClickListener)
            R.layout.eddystone_url_item -> return EddystoneUrlHolder(view, ctx, onLongClickListener)
            R.layout.ruuvitag_item -> return RuuviTagHolder(view, ctx, onLongClickListener)
            R.layout.ibeacon_altbeacon_item -> return IBeaconAltBeaconHolder(view, ctx, onLongClickListener)
            else -> return BaseHolder(view, ctx, onLongClickListener)
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        if (getItemViewType(position) != R.layout.footer_item) {
            val beacon = getItem(position)

            if (beacon != null) {
                holder.bindView(beacon)
            }
        }
    }

    interface OnControlsOpen {
        fun onOpenControls(beacon: BeaconSaved)
    }
}
