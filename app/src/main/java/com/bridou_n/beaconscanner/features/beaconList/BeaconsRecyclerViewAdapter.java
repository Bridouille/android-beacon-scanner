package com.bridou_n.beaconscanner.features.beaconList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bridou_n.beaconscanner.R;
import com.bridou_n.beaconscanner.models.BeaconSaved;
import com.bridou_n.beaconscanner.utils.CountHelper;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by bridou_n on 30/09/2016.
 */

public class BeaconsRecyclerViewAdapter extends RealmRecyclerViewAdapter<BeaconSaved, BeaconsRecyclerViewAdapter.BaseHolder> {
    private static final String     TAG = "BEACONS_RV_ADAPTER";
    private static final int        VIEW_TYPE_EDDYSTONE_UID = 1;
    private static final int        VIEW_TYPE_EDDYSTONE_URL = 2;
    private static final int        VIEW_TYPE_IBEAON_ALTBEACON = 3;
    private static final int        VIEW_TYPE_FOOTER = 4;
    private int                     expandedPosition = -1;

    private Context ctx;

    public BeaconsRecyclerViewAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<BeaconSaved> data, boolean autoUpdate) {
        super(context, data, autoUpdate);
        ctx = context;
    }

    public static class BaseHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.more_info) View moreInfo;
        @BindView(R.id.beacon_type) TextView beaconType;
        @BindView(R.id.address) TextView address;
        @BindView(R.id.distance) TextView distance;
        @BindView(R.id.distance_qualifier) TextView distanceQualifier;
        @BindView(R.id.rssi) TextView rssi;
        @BindView(R.id.tx) TextView tx;
        @BindView(R.id.manufacturer) TextView manufacturer;
        @BindView(R.id.tlm_data) LinearLayout tlmData;
        @BindView(R.id.battery) TextView battery;
        @BindView(R.id.ticks) TextView pduCount;
        @BindView(R.id.uptime) TextView uptime;
        @BindView(R.id.temperature) TextView temperature;
        @BindView(R.id.last_seen) TextView lastSeen;
        BeaconsRecyclerViewAdapter adapter;

        public BaseHolder(View itemView, BeaconsRecyclerViewAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
        }

        @OnClick(R.id.beacon_container)
        public void expandCollapseInfo(View v) {

            // Check for an expanded view, collapse if you find one
            if (adapter.expandedPosition >= 0) {
                adapter.notifyItemChanged(adapter.expandedPosition);
            }

            if (adapter.expandedPosition == getAdapterPosition()) {
                adapter.notifyItemChanged(adapter.expandedPosition);
                adapter.expandedPosition = -1;
            } else {
                // Set the current position to "expanded"
                adapter.expandedPosition = getAdapterPosition();
                adapter.notifyItemChanged(getAdapterPosition());
            }
        }
    }

    public static class EddystoneUidHolder extends BaseHolder {
        @BindView(R.id.namespace_id) TextView namespaceId;
        @BindView(R.id.instance_id) TextView instanceId;

        public EddystoneUidHolder(View itemView, BeaconsRecyclerViewAdapter adapter) {
            super(itemView, adapter);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class EddystoneUrlHolder extends BaseHolder {
        @BindView(R.id.url) TextView url;

        public EddystoneUrlHolder(View itemView, BeaconsRecyclerViewAdapter adapter) {
            super(itemView, adapter);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class IBeaconAltBeaconHolder extends BaseHolder {
        @BindView(R.id.proximity_uuid) TextView proximityUUID;
        @BindView(R.id.major) TextView major;
        @BindView(R.id.minor) TextView minor;

        public IBeaconAltBeaconHolder(View itemView, BeaconsRecyclerViewAdapter adapter) {
            super(itemView, adapter);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getData().size()) {
            return VIEW_TYPE_FOOTER;
        }
        BeaconSaved b = getItem(position);

        switch (b.getBeaconType()) {
            case BeaconSaved.TYPE_EDDYSTONE_UID:
                return VIEW_TYPE_EDDYSTONE_UID;
            case BeaconSaved.TYPE_EDDYSTONE_URL:
                return VIEW_TYPE_EDDYSTONE_URL;
            case BeaconSaved.TYPE_ALTBEACON:
            case BeaconSaved.TYPE_IBEACON:
                return VIEW_TYPE_IBEAON_ALTBEACON;
            default:
                return VIEW_TYPE_IBEAON_ALTBEACON;
        }
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_EDDYSTONE_UID:
                return new EddystoneUidHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.eddystone_uid_item, parent, false), this);
            case VIEW_TYPE_EDDYSTONE_URL:
                return new EddystoneUrlHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.eddystone_url_item, parent, false), this);
            case VIEW_TYPE_IBEAON_ALTBEACON:
                return new IBeaconAltBeaconHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ibeacon_altbeacon_item, parent, false), this);
            default:
                return new BaseHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_item, parent, false), this);
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        if (getItemViewType(position) != VIEW_TYPE_FOOTER) {
            BeaconSaved b = getItem(position);

            holder.moreInfo.setVisibility(expandedPosition == position ? View.VISIBLE : View.GONE);
            holder.address.setText(b.getBeaconAddress());
            holder.distance.setText(String.format(Locale.getDefault(), "%.2f", b.getDistance()));
            holder.distanceQualifier.setText(getDistanceQualifier(b.getDistance()));
            holder.rssi.setText(String.format(Locale.getDefault(), "%d", b.getRSSI()));
            holder.tx.setText(String.format(Locale.getDefault(), "%d", b.getTxPower()));
            holder.manufacturer.setText(String.format(Locale.getDefault(), "0x%04X", b.getManufacturer()));
            holder.lastSeen.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(b.getLastSeen()));

            if (b.isHasTelemetryData()) {
                holder.tlmData.setVisibility(View.VISIBLE);
                holder.battery.setText(String.format(Locale.getDefault(), "%d", b.getBatteryMilliVolts()));
                holder.pduCount.setText(CountHelper.coolFormat(b.getPduCount(), 0));
                holder.uptime.setText(CountHelper.coolFormat(b.getUptime(), 0));
                holder.temperature.setText(String.format(Locale.getDefault(), "%.1f", b.getTemperature()));
            } else {
                holder.tlmData.setVisibility(View.GONE);
            }

            switch (getItemViewType(position)) {
                case VIEW_TYPE_EDDYSTONE_UID:
                    EddystoneUidHolder eddyUid = (EddystoneUidHolder) holder;

                    eddyUid.beaconType.setText(String.format(Locale.getDefault(), "%s%s",
                            ctx.getString(R.string.eddystone_uid),
                            b.isHasTelemetryData() ? ctx.getString(R.string.plus_tlm) : ""));
                    eddyUid.namespaceId.setText(b.getNamespaceId());
                    eddyUid.instanceId.setText(b.getInstanceId());
                    break;
                case VIEW_TYPE_EDDYSTONE_URL:
                    EddystoneUrlHolder eddyUrl = (EddystoneUrlHolder) holder;

                    eddyUrl.beaconType.setText(String.format(Locale.getDefault(), "%s%s",
                            ctx.getString(R.string.eddystone_url),
                            b.isHasTelemetryData() ? ctx.getString(R.string.plus_tlm) : ""));
                    eddyUrl.address.setText(b.getBeaconAddress());
                    eddyUrl.url.setText(b.getURL());
                    break;
                case VIEW_TYPE_IBEAON_ALTBEACON:
                    IBeaconAltBeaconHolder h = (IBeaconAltBeaconHolder) holder;

                    h.beaconType.setText(String.format(Locale.getDefault(), "%s",
                            b.getBeaconType() == BeaconSaved.TYPE_IBEACON ? ctx.getString(R.string.ibeacon) : ctx.getString(R.string.altbeacon)));
                    h.proximityUUID.setText(b.getUUID());
                    h.major.setText(b.getMajor());
                    h.minor.setText(b.getMinor());
                    break;
            }
        }
    }

    private String getDistanceQualifier(double distance) {
        if (distance < 0.5) {
            return ctx.getString(R.string.immediate);
        } else if (distance < 5) {
            return ctx.getString(R.string.near);
        } else if (distance < 20) {
            return ctx.getString(R.string.far);
        } else {
            return ctx.getString(R.string.unknown);
        }
    }
}
