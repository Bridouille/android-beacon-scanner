package com.bridou_n.beaconscanner.features.beaconList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.*
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.CountHelper
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import java.text.DateFormat
import java.util.*

/**
 * Created by bridou_n on 30/09/2016.
 */

class BeaconsRecyclerViewAdapter(val data: RealmResults<BeaconSaved>,
                                 val ctx: Context,
                                 val onLongClickListener: OnControlsOpen?) :
        RealmRecyclerViewAdapter<BeaconSaved, BeaconsRecyclerViewAdapter.BaseHolder>(data, true) {

    companion object {
        private val TAG = "BEACONS_RV_ADAPTER"
    }

    open class BaseHolder(itemView: View, val ctx: Context, val listener: OnControlsOpen?) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.card) lateinit var cardView: CardView

        @BindView(R.id.beacon_type) lateinit var beaconType: TextView
        @BindView(R.id.address) lateinit var address: TextView
        @BindView(R.id.manufacturer) lateinit var manufacturer: TextView

        @BindView(R.id.distance) lateinit var distance: TextView
        @BindView(R.id.last_seen) lateinit var lastSeen: TextView

        @BindView(R.id.rssi) lateinit var rssi: TextView
        @BindView(R.id.tx) lateinit var tx: TextView

        @BindViews(R.id.battery_container, R.id.temperature_container, R.id.uptime_container, R.id.pdu_sent_container) lateinit var tlmData: List<@JvmSuppressWildcards LinearLayout>
        @BindView(R.id.battery) lateinit var battery: TextView
        @BindView(R.id.temperature) lateinit var temperature: TextView
        @BindView(R.id.uptime) lateinit var uptime: TextView
        @BindView(R.id.pdu_sent) lateinit var pduSent: TextView

        // Included layouts
        @BindView(R.id.ibeacon_altbeacon_item) lateinit var iBeaconLayout: View
        @BindView(R.id.eddystone_uid_item) lateinit var eddystoneUidLayout: View
        @BindView(R.id.eddystone_url_item) lateinit var eddystoneUrlLayout: View
        @BindView(R.id.ruuvitag_item) lateinit var ruuviTagLayout: View

        protected var beaconDisplayed: BeaconSaved? = null

        open fun bindView(beacon: BeaconSaved) {
            beaconDisplayed = beacon

            address.text = beacon.beaconAddress
            distance.text = String.format(Locale.getDefault(), "%.2f", beacon.distance)
            manufacturer.text = String.format(Locale.getDefault(), "0x%04X", beacon.manufacturer)
            lastSeen.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault()).format(Date(beacon.lastSeen))

            rssi.text = String.format(ctx.getString(R.string.rssi_x_dbm), beacon.rssi)
            tx.text = String.format(ctx.getString(R.string.tx_x_dbm), beacon.txPower)

            val telemetry = beacon.telemetryData
            if (telemetry != null) {
                tlmData.forEach { it.visibility = View.VISIBLE }
                battery.text = String.format(ctx.getString(R.string.battery_x_mv), telemetry.batteryMilliVolts)
                temperature.text = String.format(ctx.getString(R.string.temperature_x_degres), telemetry.temperature) // %.1f
                uptime.text = String.format(ctx.getString(R.string.uptime_x_seconds), CountHelper.coolFormat(telemetry.uptime.toDouble(), 0))
                pduSent.text = String.format(ctx.getString(R.string.x_packets_sent), CountHelper.coolFormat(telemetry.pduCount.toDouble(), 0))
            } else {
                tlmData.forEach { it.visibility = View.GONE }
            }
        }

        fun hideAllLayouts() {
            iBeaconLayout.visibility = View.GONE
            eddystoneUidLayout.visibility = View.GONE
            eddystoneUrlLayout.visibility = View.GONE
            ruuviTagLayout.visibility = View.GONE
        }

        @OnClick(R.id.visit_website_btn, R.id.ruuvi_visit_website_btn)
        fun onUrlClicked() {
            val url = beaconDisplayed?.eddystoneUrlData?.url
            try {
                val uri = Uri.parse(url)
                ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) { }
        }

        @OnLongClick(R.id.card)
        fun onLongClick(): Boolean {
            beaconDisplayed?.let {
                listener?.onOpenControls(it)
            }
            return true
        }
    }

    class IBeaconAltBeaconHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {
        @BindView(R.id.proximity_uuid) lateinit var proximityUUID: TextView
        @BindView(R.id.major) lateinit var major: TextView
        @BindView(R.id.minor) lateinit var minor: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            cardView.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.ibeaconBackground))
            hideAllLayouts()
            iBeaconLayout.visibility = View.VISIBLE

            beaconType.text = String.format(Locale.getDefault(), "%s",
                    if (beacon.beaconType == BeaconSaved.TYPE_IBEACON) itemView.context.getString(R.string.ibeacon)
                    else itemView.context.getString(R.string.altbeacon))

            val ibeaconData = beacon.ibeaconData
            if (ibeaconData != null) {
                proximityUUID.text = String.format(ctx.getString(R.string.uuid_x), ibeaconData.uuid)
                major.text = String.format(ctx.getString(R.string.major_x), ibeaconData.major)
                minor.text = String.format(ctx.getString(R.string.minor_x), ibeaconData.minor)
            }
        }
    }

    class EddystoneUidHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {
        @BindView(R.id.namespace_id) lateinit var namespaceId: TextView
        @BindView(R.id.instance_id) lateinit var instanceId: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            cardView.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.eddystoneUidBackground))
            hideAllLayouts()
            eddystoneUidLayout.visibility = View.VISIBLE

            beaconType.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_uid),
                    if (beacon.telemetryData != null) itemView.context.getString(R.string.plus_tlm) else "")
            val eddystoneUidData = beacon.eddystoneUidData
            if (eddystoneUidData != null) {
                namespaceId.text = String.format(ctx.getString(R.string.namespace_id_x), eddystoneUidData.namespaceId)
                instanceId.text = String.format(ctx.getString(R.string.instance_id_x), eddystoneUidData.instanceId)
            }
        }
    }

    class EddystoneUrlHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {
        @BindView(R.id.url) lateinit var url: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            cardView.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.eddystoneUrlBackground))
            hideAllLayouts()
            eddystoneUrlLayout.visibility = View.VISIBLE

            beaconType.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_url),
                    if (beacon.telemetryData != null) itemView.context.getString(R.string.plus_tlm) else "")
            url.text = String.format(ctx.getString(R.string.url_x), beacon.eddystoneUrlData?.url)
        }
    }

    class RuuviTagHolder(itemView: View, ctx: Context, listener: OnControlsOpen?) : BaseHolder(itemView, ctx, listener) {
        @BindView(R.id.ruuvi_url) lateinit var ruuviUrl: TextView
        @BindView(R.id.ruuvi_air_pressure) lateinit var ruuviAirPressure: TextView
        @BindView(R.id.ruuvi_temperature) lateinit var ruuviTemperature: TextView
        @BindView(R.id.ruuvi_humidity) lateinit var ruuviHumidity: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)

            cardView.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.ruuvitagBackground))
            hideAllLayouts()
            ruuviTagLayout.visibility = View.VISIBLE

            beaconType.text = String.format(Locale.getDefault(), "%s", itemView.context.getString(R.string.ruuvitag))
            ruuviUrl.text = String.format(ctx.getString(R.string.url_x), beacon.eddystoneUrlData?.url)

            val ruuviData = beacon.ruuviData
            if (ruuviData != null) {
                ruuviAirPressure.text = String.format(ctx.getString(R.string.air_pressure_x_hpa), ruuviData.airPressure)
                ruuviTemperature.text = String.format(ctx.getString(R.string.temperature_d_degres), ruuviData.temperatue)
                ruuviHumidity.text = String.format(ctx.getString(R.string.humidity_x_percent), ruuviData.humidity)
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
