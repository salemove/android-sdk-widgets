package com.glia.widgets.view.floatingvisitorvideoview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import com.glia.androidsdk.comms.VideoView
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.R
import com.glia.widgets.helper.logCallScreenButtonClicked
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract.FlipButtonState
import com.glia.widgets.view.unifiedui.applyBarButtonStyleTheme
import com.glia.widgets.view.unifiedui.theme.call.VisitorVideoTheme
import com.google.android.material.card.MaterialCardView

/**
 * @hide
 */
internal class FloatingVisitorVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    internal var onFlipButtonClickListener: OnClickListener? = null

    private var videoView: VideoView? = null
    private var onHoldOverlay: TextView
    private var flipCameraButtonContainer: View
    private var flipCameraImageButton: ImageButton
    private var sendFlipButtonAccessibilityEvent = false

    init {
        LayoutInflater.from(context).inflate(R.layout.visitor_video_floating_view, this)
        onHoldOverlay = findViewById(R.id.on_hold_textview)
        onHoldOverlay.setLocaleText(R.string.general_you)
        flipCameraImageButton = findViewById(R.id.flip_camera_image_button)
        flipCameraButtonContainer = findViewById(R.id.flip_camera_button)
        flipCameraButtonContainer.setOnClickListener {
            sendFlipButtonAccessibilityEvent = true
            onFlipButtonClickListener?.onClick(it)
            GliaLogger.logCallScreenButtonClicked(ButtonNames.FLIP_CAMERA)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeView(videoView)
        releaseVideoStream()
    }

    fun onResume() {
        resumeVideoStream()
    }

    fun onPause() {
        pauseVideoStream()
    }

    fun showVisitorVideo(newVideoView: VideoView) {
        videoView = newVideoView
        newVideoView.setZOrderMediaOverlay(true)
        removeVideoView()
        addView(newVideoView, 0)
        GliaLogger.i(LogEvents.CALL_SCREEN_VISITOR_VIDEO_SHOWN)
    }

    fun showFlipCameraButton(flipButtonState: FlipButtonState) {
        when (flipButtonState) {
            FlipButtonState.HIDE -> flipCameraButtonContainer.isVisible = false

            FlipButtonState.SWITCH_TO_FACE_CAMERA -> {
                setFlipCameraButton(R.string.call_visitor_video_front_camera_button_accessibility_label)
            }

            FlipButtonState.SWITCH_TO_BACK_CAMERA -> {
                setFlipCameraButton(R.string.call_visitor_video_back_camera_button_accessibility_label)
            }
        }
    }

    private fun setFlipCameraButton(contentDescriptionRes: Int) {
        flipCameraButtonContainer.isVisible = true
        flipCameraButtonContainer.setLocaleContentDescription(contentDescriptionRes)
        if (sendFlipButtonAccessibilityEvent) {
            sendFlipButtonAccessibilityEvent = false
            flipCameraButtonContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
        }
    }

    fun hideVisitorVideo() {
        removeView(videoView)
        releaseVideoStream()
    }

    fun showOnHold() {
        pauseVideoStream()
        onHoldOverlay.visibility = VISIBLE
    }

    fun hideOnHold() {
        resumeVideoStream()
        onHoldOverlay.visibility = GONE
    }

    fun setTheme(visitorVideo: VisitorVideoTheme?) {
        flipCameraImageButton.applyBarButtonStyleTheme(visitorVideo?.flipCameraButton)
    }

    private fun removeVideoView() {
        (get(0) as? VideoView)?.also { this.removeView(it) }
    }

    private fun releaseVideoStream() {
        videoView?.release()
        videoView = null
    }

    private fun pauseVideoStream() {
        videoView?.pauseRendering()
    }

    private fun resumeVideoStream() {
        videoView?.resumeRendering()
    }

}
