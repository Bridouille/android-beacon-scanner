package com.bridou_n.beaconscanner.features.beaconList

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.copyPaste.RoundedBsDialog
import com.bridou_n.beaconscanner.utils.extensionFunctions.getBeaconWithId
import com.bridou_n.beaconscanner.utils.extensionFunctions.showSnackBar
import io.realm.Realm
import javax.inject.Inject


/**
 * Created by bridou_n on 23/10/2017.
 */

class ControlsBottomSheetDialog : RoundedBsDialog() {

    companion object {
        const val KEY_BEACON = "key_beacon"
        const val KEY_BLOCKED = "key_blocked"

        fun newInstance(beacon: BeaconSaved, blocked: Boolean = false) : ControlsBottomSheetDialog {
            return ControlsBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_BEACON, beacon)
                    putBoolean(KEY_BLOCKED, blocked)
                }
            }
        }
    }

    private lateinit var beacon: BeaconSaved
    private var isBlockedLst: Boolean = false

    @Inject lateinit var realm: Realm

    private fun restoreFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            beacon = bundle.getParcelable(KEY_BEACON)
            isBlockedLst = bundle.getBoolean(KEY_BLOCKED)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_BEACON, beacon)
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

        val hashcode = beacon.hashcode

        if (isBlockedLst) {
            removeContainer.visibility = View.GONE
            blockLabel.setText(R.string.unblock)
        }

        removeContainer.setOnClickListener {
            realm.executeTransactionAsync(Realm.Transaction { tRealm ->
                tRealm.getBeaconWithId(hashcode)?.deleteFromRealm()
            }, Realm.Transaction.OnSuccess {
                dismiss()
            })
        }

        clipboardContainer.setOnClickListener {
            context?.let {
                val clipboard = it.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Beacon infos", beacon.toString())
                clipboard.primaryClip = clip

                dismiss()
                (activity as AppCompatActivity).showSnackBar(it.getString(R.string.the_informations_has_been_copied))
            }
        }

        blockedContainer.setOnClickListener {
            realm.executeTransactionAsync(Realm.Transaction { tRealm ->
                val beacon = tRealm.getBeaconWithId(hashcode)

                beacon?.isBlocked = !isBlockedLst
            }, Realm.Transaction.OnSuccess {
                dismiss()
            })
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        realm.close()
        super.onDismiss(dialog)
    }
}