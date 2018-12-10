package com.bridou_n.beaconscanner.features.blockedList

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.beaconList.BeaconsRecyclerViewAdapter
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.bridou_n.beaconscanner.utils.extensionFunctions.getScannedBeacons
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_blocked.*
import javax.inject.Inject

class BlockedActivity : AppCompatActivity() {

    @Inject lateinit var realm: Realm

    lateinit var beaconResults: RealmResults<BeaconSaved>

    private val rvAdapter by lazy {
        BeaconsRecyclerViewAdapter(this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
            override fun onOpenControls(beacon: BeaconSaved) {
                ControlsBottomSheetDialog.newInstance(beacon, true).apply {
                    show(supportFragmentManager, this.tag)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)
        component().inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.blacklist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        beaconResults = realm.getScannedBeacons(true)
        beacons_rv.layoutManager = LinearLayoutManager(this)
        beacons_rv.setHasFixedSize(true)
        beacons_rv.adapter = rvAdapter

        beaconResults.addChangeListener { results ->
            if (results.isLoaded) {
                showEmptyView(results.size == 0)
                rvAdapter.submitList(results)
            }
        }
    }

    fun showEmptyView(show: Boolean) {
        empty_view.visibility = if (show) View.VISIBLE else View.GONE
        beacons_rv.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
