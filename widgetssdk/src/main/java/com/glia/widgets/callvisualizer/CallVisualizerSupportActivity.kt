package com.glia.widgets.callvisualizer

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import kotlinx.parcelize.Parcelize

class CallVisualizerSupportActivity : AppCompatActivity() {
    companion object {
        val PERMISSION_TYPE_TAG: String = CallVisualizerController::class.java.simpleName
    }
}

@Parcelize
sealed class PermissionType : Parcelable
object ScreenSharing : PermissionType()
object Camera : PermissionType()
object None : PermissionType()
