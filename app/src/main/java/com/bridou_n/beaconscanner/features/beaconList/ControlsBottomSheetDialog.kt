package com.bridou_n.beaconscanner.features.beaconList

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import com.bridou_n.beaconscanner.utils.extensionFunctions.getBeaconWithId
import com.bridou_n.beaconscanner.utils.extensionFunctions.showSnackBar
import io.realm.Realm
import javax.inject.Inject


/**
 * Created by bridou_n on 23/10/2017.
 */

class ControlsBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val KEY_BEACON = "key_beacon"
        const val KEY_BLOCKED = "key_blocked"

        fun newInstance(beacon: BeaconSaved, blocked: Boolean = false) : ControlsBottomSheetDialog {
            val frag = ControlsBottomSheetDialog()

            val bundle = Bundle()
            bundle.putParcelable(KEY_BEACON, beacon)
            bundle.putBoolean(KEY_BLOCKED, blocked)
            frag.arguments = bundle

            return frag
        }
    }

    private val TAG = "ConstrolsBS"
    private lateinit var beacon: BeaconSaved
    private var isBlockedLst: Boolean = false

    @Inject lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreFromBundle(savedInstanceState ?: arguments)
        AppSingleton.activityComponent.inject(this)
    }

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

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val contentView = View.inflate(context, R.layout.bottom_sheet_controls, null)
        dialog.setContentView(contentView)

        // This is to set the Bottomsheet to STATE_EXPANDED upon show
        dialog.setOnShowListener { dial ->
            val bsDialog = dial as BottomSheetDialog
            val bottomSheet = bsDialog.findViewById<FrameLayout>(android.support.design.R.id.design_bottom_sheet)

            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }

        setupView(contentView)
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
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Beacon infos", beacon.toString())
            clipboard.primaryClip = clip

            dismiss()
            (activity as AppCompatActivity).showSnackBar(context.getString(R.string.the_informations_has_been_copied))
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