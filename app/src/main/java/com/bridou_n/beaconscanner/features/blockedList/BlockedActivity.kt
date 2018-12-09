package com.bridou_n.beaconscanner.features.blockedList

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
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
        beacons_rv.adapter = BeaconsRecyclerViewAdapter(beaconResults, this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
            override fun onOpenControls(beacon: BeaconSaved) {
                val bsDialog = ControlsBottomSheetDialog.newInstance(beacon, true)
                bsDialog.show(supportFragmentManager, bsDialog.tag)
            }
        })

        beaconResults.addChangeListener { results ->
            if (results.isLoaded) {
                showEmptyView(results.size == 0)
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
