package com.glia.widgets.view.floatingvisitorvideoview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.R
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.view.SimpleTouchListener
import com.glia.widgets.view.unifiedui.theme.call.VisitorVideoTheme

/**
 * @hide
 */
internal class FloatingVisitorVideoContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), FloatingVisitorVideoContract.View {
    private var floatingVisitorVideoView: FloatingVisitorVideoView
    private var mediaStateId: Int? = null
    internal var onFlipButtonClickListener: OnClickListener?
        set(value) {
            floatingVisitorVideoView.onFlipButtonClickListener = value
        }
        get() = floatingVisitorVideoView.onFlipButtonClickListener

    init {
        LayoutInflater.from(context).inflate(R.layout.visitor_video_layout_view, this)
        floatingVisitorVideoView = findViewById(R.id.visitor_video_card)
        floatingVisitorVideoView.setLocaleContentDescription(R.string.call_visitor_video_accessibility_label)
        setVisitorVideoContainerTouchListener()
        hide()
    }

    override fun show(state: MediaState?) {
        if (state != null && isNewMediaState(state)) {
            floatingVisitorVideoView.showVisitorVideo(
                state.video.createVideoView(context.asActivity())
            )
        }
        visibility = VISIBLE
    }

    override fun showFlipCameraButton(flipButtonState: FloatingVisitorVideoContract.FlipButtonState) {
        floatingVisitorVideoView.showFlipCameraButton(flipButtonState)
    }

    override fun hide() {
        mediaStateId = null
        floatingVisitorVideoView.hideVisitorVideo()
        visibility = GONE
    }

    override fun onResume() {
        floatingVisitorVideoView.onResume()
    }

    override fun onPause() {
        floatingVisitorVideoView.onPause()
    }

    override fun showOnHold() {
        floatingVisitorVideoView.showOnHold()
    }

    override fun hideOnHold() {
        floatingVisitorVideoView.hideOnHold()
    }

    internal fun setTheme(visitorVideo: VisitorVideoTheme?) {
        floatingVisitorVideoView.setTheme(visitorVideo)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setVisitorVideoContainerTouchListener() {
        floatingVisitorVideoView.setOnTouchListener(
            SimpleTouchListener(
                { viewLocation },
                { newPositionX: Float, newPositionY: Float ->
                    onViewDragged(
                        newPositionX,
                        newPositionY
                    )
                }
            ))
    }

    private val viewLocation: PointF
        get() = PointF(floatingVisitorVideoView.x, floatingVisitorVideoView.y)

    private fun onViewDragged(positionX: Float, positionY: Float) {
        var newPositionX = positionX
        var newPositionY = positionY
        if (newPositionX < 0) {
            newPositionX = 0f
        }
        if (newPositionY < 0) {
            newPositionY = 0f
        }
        if (newPositionX > width - floatingVisitorVideoView.width) {
            newPositionX = (width - floatingVisitorVideoView.width).toFloat()
        }
        if (newPositionY > height - floatingVisitorVideoView.height) {
            newPositionY = (height - floatingVisitorVideoView.height).toFloat()
        }
        setFloatingViewPosition(newPositionX, newPositionY)
    }

    private fun setFloatingViewPosition(newPositionX: Float, newPositionY: Float) {
        floatingVisitorVideoView.x = newPositionX
        floatingVisitorVideoView.y = newPositionY
        floatingVisitorVideoView.invalidate()
    }

    private fun isNewMediaState(newState: MediaState): Boolean {
        val newStateId = System.identityHashCode(newState)
        if (newStateId != mediaStateId) {
            mediaStateId = newStateId
            return true
        }
        return false
    }
}
