package com.glia.widgets.callvisualizer

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import kotlinx.parcelize.Parcelize

class CallVisualizerSupportActivity : AppCompatActivity() {
    companion object {
        val PERMISSION_TYPE_TAG: String = CallVisualizerContract.Controller::class.java.simpleName
    }
}

@Parcelize
internal sealed class PermissionType : Parcelable
internal object ScreenSharing : PermissionType()
internal object Camera : PermissionType()
internal object None : PermissionType()
