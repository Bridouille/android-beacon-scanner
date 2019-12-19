package com.bridou_n.beaconscanner.utils.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.bridou_n.beaconscanner.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class RoundedBottomSheetDialog : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    fun show(supportFragmentManager: FragmentManager) {
        show(supportFragmentManager, this.tag)
    }
}