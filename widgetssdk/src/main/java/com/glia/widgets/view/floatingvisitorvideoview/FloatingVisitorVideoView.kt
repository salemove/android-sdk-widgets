package com.glia.widgets.view.floatingvisitorvideoview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import com.glia.androidsdk.comms.VideoView
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.applyBarButtonStyleTheme
import com.glia.widgets.view.unifiedui.theme.call.VisitorVideoTheme
import com.google.android.material.card.MaterialCardView

/**
 * @hide
 */
class FloatingVisitorVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    internal var onFlipButtonClickListener: OnClickListener? = null

    private var videoView: VideoView? = null
    private var onHoldOverlay: TextView
    private var flipCameraButtonContainer: View
    private var flipCameraImageButton: ImageButton
    private val stringProvider = Dependencies.getStringProvider()

    init {
        LayoutInflater.from(context).inflate(R.layout.visitor_video_floating_view, this)
        onHoldOverlay = findViewById(R.id.on_hold_textview)
        onHoldOverlay.text = stringProvider.getRemoteString(R.string.general_you)
        flipCameraImageButton = findViewById(R.id.flip_camera_image_button)
        flipCameraButtonContainer = findViewById(R.id.flip_camera_button)
        flipCameraButtonContainer.setOnClickListener { onFlipButtonClickListener?.onClick(it) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseVideoStream()
    }

    fun onResume() {
        resumeVideoStream()
    }

    fun onPause() {
        pauseVideoStream()
    }

    fun showVisitorVideo(newVideoView: VideoView?) {
        videoView = newVideoView
        videoView!!.setZOrderMediaOverlay(true)
        removeVideoView()
        addView(videoView, 0)
    }

    fun showFlipCameraButton(flipButtonState: FlipButtonState) {
        when (flipButtonState) {
            FlipButtonState.HIDE -> flipCameraButtonContainer.isVisible = false
            FlipButtonState.SWITCH_TO_FACE_CAMERA -> {
                flipCameraButtonContainer.isVisible = true
            }
            FlipButtonState.SWITCH_TO_BACK_CAMERA -> {
                flipCameraButtonContainer.isVisible = true
            }
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

    internal fun setTheme(visitorVideo: VisitorVideoTheme?) {
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

    enum class FlipButtonState {
        SWITCH_TO_FACE_CAMERA,
        SWITCH_TO_BACK_CAMERA,
        HIDE
    }

}
