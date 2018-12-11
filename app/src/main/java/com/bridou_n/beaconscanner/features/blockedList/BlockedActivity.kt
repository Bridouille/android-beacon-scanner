package com.bridou_n.beaconscanner.features.blockedList

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bridou_n.beaconscanner.Database.AppDatabase
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.features.beaconList.BeaconsRecyclerViewAdapter
import com.bridou_n.beaconscanner.features.beaconList.ControlsBottomSheetDialog
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.extensionFunctions.component
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_blocked.*
import javax.inject.Inject

class BlockedActivity : AppCompatActivity() {

    @Inject lateinit var db: AppDatabase

    private val rvAdapter by lazy {
        BeaconsRecyclerViewAdapter(this, object : BeaconsRecyclerViewAdapter.OnControlsOpen {
            override fun onOpenControls(beacon: BeaconSaved) {
                ControlsBottomSheetDialog.newInstance(beacon.hashcode, true).apply {
                    show(supportFragmentManager, this.tag)
                }
            }
        })
    }
    private var dbQuery: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)
        component().inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.blacklist)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        beacons_rv.apply {
            layoutManager = LinearLayoutManager(this@BlockedActivity)
            setHasFixedSize(true)
            adapter = rvAdapter
        }

        dbQuery = db.beaconsDao().getBeacons(blocked = true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showEmptyView(it.size == 0)
                    rvAdapter.submitList(it)
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

    override fun onDestroy() {
        super.onDestroy()
        dbQuery?.dispose()
    }
}
