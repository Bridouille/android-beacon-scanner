package com.bridou_n.beaconscanner.features.beaconList

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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

class BeaconsRecyclerViewAdapter(val data: RealmResults<BeaconSaved>) :
        RealmRecyclerViewAdapter<BeaconSaved, BeaconsRecyclerViewAdapter.BaseHolder>(data, true) {

    companion object {
        private val TAG = "BEACONS_RV_ADAPTER"
    }

    private var expandedPosition = -1

    open class BaseHolder(itemView: View, internal var adapter: BeaconsRecyclerViewAdapter) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.more_info) lateinit var moreInfo: View
        @BindView(R.id.beacon_type) lateinit var beaconType: TextView
        @BindView(R.id.address) lateinit var address: TextView
        @BindView(R.id.distance) lateinit var distance: TextView
        @BindView(R.id.distance_qualifier) lateinit var distanceQualifier: TextView
        @BindView(R.id.rssi) lateinit var rssi: TextView
        @BindView(R.id.tx) lateinit var tx: TextView
        @BindView(R.id.manufacturer) lateinit var manufacturer: TextView
        @BindView(R.id.tlm_data) lateinit var tlmData: ConstraintLayout
        @BindView(R.id.battery) lateinit var battery: TextView
        @BindView(R.id.ticks) lateinit var pduCount: TextView
        @BindView(R.id.uptime) lateinit var uptime: TextView
        @BindView(R.id.temperature) lateinit var temperature: TextView
        @BindView(R.id.last_seen) lateinit var lastSeen: TextView

        @OnClick(R.id.beacon_container)
        fun expandCollapseInfo() {

            // Check for an expanded view, collapse if you find one
            if (adapter.expandedPosition >= 0) {
                adapter.notifyItemChanged(adapter.expandedPosition)
            }

            if (adapter.expandedPosition == adapterPosition) {
                adapter.notifyItemChanged(adapter.expandedPosition)
                adapter.expandedPosition = -1
            } else {
                // Set the current position to "expanded"
                adapter.expandedPosition = adapterPosition
                adapter.notifyItemChanged(adapterPosition)
            }
        }

        open fun bindView(beacon: BeaconSaved) {
            address.text = beacon.beaconAddress
            distance.text = String.format(Locale.getDefault(), "%.2f", beacon.distance)
            distanceQualifier.text = getDistanceQualifier(beacon.distance)
            rssi.text = String.format(Locale.getDefault(), "%d", beacon.rssi)
            tx.text = String.format(Locale.getDefault(), "%d", beacon.txPower)
            manufacturer.text = String.format(Locale.getDefault(), "0x%04X", beacon.manufacturer)
            lastSeen.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(Date(beacon.lastSeen))

            if (beacon.hasTelemetryData) {
                tlmData.visibility = View.VISIBLE
                battery.text = String.format(Locale.getDefault(), "%d", beacon.batteryMilliVolts)
                pduCount.text = CountHelper.coolFormat(beacon.pduCount.toDouble(), 0)
                uptime.text = CountHelper.coolFormat(beacon.uptime.toDouble(), 0)
                temperature.text = String.format(Locale.getDefault(), "%.1f", beacon.getTemperature())
            } else {
                tlmData.visibility = View.GONE
            }
        }

        fun setExpanded(state: Boolean) {
            moreInfo.visibility = if (state) View.VISIBLE else View.GONE
        }

        private fun getDistanceQualifier(distance: Double): String {
            if (distance < 0.5) {
                return itemView.context.getString(R.string.immediate)
            } else if (distance < 5) {
                return itemView.context.getString(R.string.near)
            } else if (distance < 20) {
                return itemView.context.getString(R.string.far)
            } else {
                return itemView.context.getString(R.string.unknown)
            }
        }
    }

    class EddystoneUidHolder(itemView: View, adapter: BeaconsRecyclerViewAdapter) : BaseHolder(itemView, adapter) {
        @BindView(R.id.namespace_id) lateinit var namespaceId: TextView
        @BindView(R.id.instance_id) lateinit var instanceId: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)
            beaconType.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_uid),
                    if (beacon.hasTelemetryData) itemView.context.getString(R.string.plus_tlm) else "")
            namespaceId.text = beacon.namespaceId
            instanceId.text = beacon.instanceId
        }
    }

    class EddystoneUrlHolder(itemView: View, adapter: BeaconsRecyclerViewAdapter) : BaseHolder(itemView, adapter) {
        @BindView(R.id.url) lateinit var url: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)
            beaconType.text = String.format(Locale.getDefault(), "%s%s",
                    itemView.context.getString(R.string.eddystone_url),
                    if (beacon.hasTelemetryData) itemView.context.getString(R.string.plus_tlm) else "")
            address.text = beacon.beaconAddress
            url.text = beacon.url
        }
    }

    class IBeaconAltBeaconHolder(itemView: View, adapter: BeaconsRecyclerViewAdapter) : BaseHolder(itemView, adapter) {
        @BindView(R.id.proximity_uuid) lateinit var proximityUUID: TextView
        @BindView(R.id.major) lateinit var major: TextView
        @BindView(R.id.minor) lateinit var minor: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bindView(beacon: BeaconSaved) {
            super.bindView(beacon)
            beaconType.text = String.format(Locale.getDefault(), "%s",
                    if (beacon.beaconType == BeaconSaved.TYPE_IBEACON) itemView.context.getString(R.string.ibeacon)
                    else itemView.context.getString(R.string.altbeacon))
            proximityUUID.text = beacon.uuid
            major.text = beacon.major
            minor.text = beacon.minor
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
            BeaconSaved.TYPE_ALTBEACON, BeaconSaved.TYPE_IBEACON -> return R.layout.ibeacon_altbeacon_item
            else -> return R.layout.ibeacon_altbeacon_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        when (viewType) {
            R.layout.eddystone_uid_item -> return EddystoneUidHolder(view, this)
            R.layout.eddystone_url_item -> return EddystoneUrlHolder(view, this)
            R.layout.ibeacon_altbeacon_item -> return IBeaconAltBeaconHolder(view, this)
            else -> return BaseHolder(view, this)
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        if (getItemViewType(position) != R.layout.footer_item) {
            val beacon = getItem(position)

            if (beacon != null) {
                holder.bindView(beacon)
                holder.setExpanded(expandedPosition == position)
            }
        }
    }
}
