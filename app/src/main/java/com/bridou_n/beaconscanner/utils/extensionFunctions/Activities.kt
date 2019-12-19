package com.bridou_n.beaconscanner.utils.extensionFunctions

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.bridou_n.beaconscanner.AppSingleton
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by bridou_n on 21/08/2017.
 */

fun AppCompatActivity.component() = AppSingleton.appComponent

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

fun ActionBar?.setHomeIcon(@DrawableRes drawableRes: Int, @ColorRes iconColor: Int) {
    if (this == null) return

    setDisplayHomeAsUpEnabled(true)
    setDisplayShowHomeEnabled(true)

    val drawable = AppCompatResources.getDrawable(this.themedContext, drawableRes)

    drawable?.let {
        val newDrawable = it.mutate()
        newDrawable.setColorFilter(ContextCompat.getColor(this.themedContext, iconColor), PorterDuff.Mode.SRC_IN)
        setHomeAsUpIndicator(newDrawable)
    }
}

fun AppCompatActivity.isPermissionGranted(permissionName: String) : Boolean {
    return ContextCompat.checkSelfPermission(this, permissionName) == PackageManager.PERMISSION_GRANTED
}

fun AppCompatActivity.reqPermission(permissionName: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permissionName), requestCode)
}

infix fun Disposable.addTo(collection: CompositeDisposable) {
    collection.add(this)
}