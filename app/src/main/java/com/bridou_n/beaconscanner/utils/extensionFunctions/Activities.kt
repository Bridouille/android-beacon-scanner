package com.bridou_n.beaconscanner.utils.extensionFunctions

import android.content.Context
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.bridou_n.beaconscanner.AppSingleton

/**
 * Created by bridou_n on 21/08/2017.
 */

fun AppCompatActivity.component() = AppSingleton.activityComponent

fun AppCompatActivity.setStatusBarColor(color: Int) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, color)
    }
}

fun AppCompatActivity.showSnackBar(str: String) = showSnackbar(findViewById(android.R.id.content), str)

fun AppCompatActivity.showSnackbar(view: View, str: String) = Snackbar.make(view, str, Snackbar.LENGTH_LONG).show()

fun AppCompatActivity.hideKeyboard() {
    // Check if no view has focus:
    val view = this.currentFocus

    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}