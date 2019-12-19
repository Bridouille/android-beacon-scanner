package com.bridou_n.beaconscanner.features.beaconList

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.Database.AppDatabase
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.utils.dialogs.RoundedBottomSheetDialog
import com.bridou_n.beaconscanner.utils.extensionFunctions.showSnackBar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * Created by bridou_n on 23/10/2017.
 */

class ControlsBottomSheetDialog : RoundedBottomSheetDialog() {

    companion object {
        const val KEY_BEACON_ID = "key_beacon_id"
        const val KEY_BLOCKED = "key_blocked"

        fun newInstance(beaconId: Int, blocked: Boolean = false) : ControlsBottomSheetDialog {
            return ControlsBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putInt(KEY_BEACON_ID, beaconId)
                    putBoolean(KEY_BLOCKED, blocked)
                }
            }
        }
    }

    private var beaconId: Int = 0
    private var isBlockedLst: Boolean = false

    private val queries = CompositeDisposable()

    @Inject lateinit var db: AppDatabase

    private fun restoreFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            beaconId = bundle.getInt(KEY_BEACON_ID)
            isBlockedLst = bundle.getBoolean(KEY_BLOCKED)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_BEACON_ID, beaconId)
        outState.putBoolean(KEY_BLOCKED, isBlockedLst)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreFromBundle(savedInstanceState ?: arguments)
        AppSingleton.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView(view)
    }

    fun setupView(contentView: View) {
        val removeContainer = contentView.findViewById<LinearLayout>(R.id.remove)
        val clipboardContainer = contentView.findViewById<LinearLayout>(R.id.clipboard)
        val blockedContainer = contentView.findViewById<LinearLayout>(R.id.block)
        val blockLabel = contentView.findViewById<TextView>(R.id.block_label)

        if (isBlockedLst) {
            removeContainer.visibility = View.GONE
            blockLabel.setText(R.string.unblock)
        }

        removeContainer.setOnClickListener {
            queries.add(Completable.fromCallable {
                db.beaconsDao().deleteBeaconById(beaconId)
            }.subscribeOn(Schedulers.io()).subscribe {
                dismissAllowingStateLoss()
            })
        }

        clipboardContainer.setOnClickListener {
            context?.let { ctx ->
                queries.add(
                    db.beaconsDao().getBeaconById(beaconId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            val clipboard = ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Beacon infos", it.toJson())
                            clipboard.primaryClip = clip

                            dismissAllowingStateLoss()
                            (activity as? BeaconListActivity)?.showGenericError(ctx.getString(R.string.the_informations_has_been_copied)) ?:
                            (activity as? AppCompatActivity)?.showSnackBar(ctx.getString(R.string.the_informations_has_been_copied))
                        }, { err ->

                        })
                )
            }
        }

        blockedContainer.setOnClickListener {
            queries.add(db.beaconsDao().getBeaconById(beaconId)
                .flatMapCompletable {
                    Completable.fromCallable{
                        db.beaconsDao().insertBeacon(it.copy(isBlocked = !isBlockedLst))
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dismissAllowingStateLoss()
                })
        }
    }

    override fun onDestroy() {
        queries.clear()
        super.onDestroy()
    }
}