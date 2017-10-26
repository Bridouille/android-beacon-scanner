package com.bridou_n.beaconscanner.features.beaconList

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bridou_n.beaconscanner.AppSingleton
import com.bridou_n.beaconscanner.R
import com.bridou_n.beaconscanner.models.BeaconSaved
import io.realm.Realm
import javax.inject.Inject

/**
 * Created by bridou_n on 23/10/2017.
 */

class ControlsBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val KEY_BEACON = "key_beacon"

        fun newInstance(beacon: BeaconSaved) : ControlsBottomSheetDialog {
            val frag = ControlsBottomSheetDialog()

            val bundle = Bundle()
            bundle.putParcelable(KEY_BEACON, beacon)
            frag.arguments = bundle

            return frag
        }
    }

    private val TAG = "ConstrolsBS"
    private lateinit var beacon: BeaconSaved

    @Inject lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreFromBundle(savedInstanceState ?: arguments)
        AppSingleton.activityComponent.inject(this)
    }

    private fun restoreFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            beacon = bundle.getParcelable(KEY_BEACON)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_BEACON, beacon)
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

        val hashcode = beacon.hashcode

        removeContainer.setOnClickListener {
            realm.executeTransactionAsync(Realm.Transaction { tRealm ->
                tRealm.where(BeaconSaved::class.java).equalTo("hashcode", hashcode)
                        .findFirst()?.deleteFromRealm()
            }, Realm.Transaction.OnSuccess {
                dismiss()
            })
        }

        clipboardContainer.setOnClickListener {

        }

        blockedContainer.setOnClickListener {
            realm.executeTransactionAsync { tRealm ->
                val beacon = tRealm.where(BeaconSaved::class.java).equalTo("hashcode", hashcode)
                        .findFirst()

                beacon?.isBlocked = true
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        realm.close()
        super.onDismiss(dialog)
    }
}