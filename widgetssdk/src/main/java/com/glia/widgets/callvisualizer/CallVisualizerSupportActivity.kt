package com.glia.widgets.callvisualizer

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import kotlinx.parcelize.Parcelize

/**
 * Glia internal class.
 *
 * Will be automatically added to integrator Manifest by Manifest merger during compilation.
 *
 * This is helper activity used to display Glia dialogs inside integrators app when current activity
 * has no material design (AppCompatActivity) support which is required for Glia dialogs.
 */
internal class CallVisualizerSupportActivity : AppCompatActivity() {
    companion object {
        val PERMISSION_TYPE_TAG: String = CallVisualizerContract.Controller::class.java.simpleName
    }
}

@Parcelize
internal sealed class PermissionType : Parcelable
internal object ScreenSharing : PermissionType()
internal object Camera : PermissionType()
internal object None : PermissionType()
