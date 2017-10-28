package com.bridou_n.beaconscanner.features.blockedList

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.beaconList.BeaconsRecyclerViewAdapter
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import com.bridou_n.beaconscanner.utils.extensionFunctions.getScannedBeacons
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject

class BlockedActivity : AppCompatActivity() {

    companion object {
        const val TAG = "BlockedActivity"
    }

    @Inject lateinit var realm: Realm

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.empty_view) lateinit var emptyView: ConstraintLayout
    @BindView(R.id.beacons_rv) lateinit var beaconsRv: RecyclerView

    lateinit var beaconResults: RealmResults<BeaconSaved>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)
        ButterKnife.bind(this)
        component().inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.blacklist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        beaconResults = realm.getScannedBeacons(true)
        beaconsRv.layoutManager = LinearLayoutManager(this)
        beaconsRv.setHasFixedSize(true)
        beaconsRv.adapter = BeaconsRecyclerViewAdapter(beaconResults, this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
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
        emptyView.visibility = if (show) View.VISIBLE else View.GONE
        beaconsRv.visibility = if (show) View.GONE else View.VISIBLE
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
