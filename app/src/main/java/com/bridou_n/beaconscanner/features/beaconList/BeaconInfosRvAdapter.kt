package com.bridou_n.beaconscanner.features.beaconList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.utils.extensionFunctions.addRipple
import kotlinx.android.synthetic.main.item_beacon_info.view.*

typealias OnBeaconInfoClick = () -> Unit

class BeaconInfosRvAdapter : ListAdapter<BeaconInfo, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BeaconInfo>() {
            override fun areItemsTheSame(oldItem: BeaconInfo, newItem: BeaconInfo) = oldItem == newItem
            override fun areContentsTheSame(oldItem: BeaconInfo, newItem: BeaconInfo) = oldItem == newItem
        }
    }

    inner class LegItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: BeaconInfo) {
            itemView.item_title.text = item.title
            itemView.item_content.text = item.content

            item.onItemClicked?.let { listener ->
                itemView.isClickable = true
                itemView.addRipple()
                itemView.setOnClickListener {
                    listener()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_beacon_info, parent, false)

        return LegItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? LegItemViewHolder)?.bind(getItem(position))
    }
}

data class BeaconInfo(
    val title: String,
    val content: String,
    val onItemClicked: OnBeaconInfoClick? = null
)