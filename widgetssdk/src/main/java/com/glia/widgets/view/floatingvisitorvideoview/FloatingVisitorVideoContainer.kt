package com.glia.widgets.view.floatingvisitorvideoview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Pair
import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.asActivity
import com.glia.widgets.view.ViewHelpers
import com.glia.widgets.view.unifiedui.theme.call.VisitorVideoTheme

/**
 * @hide
 */
class FloatingVisitorVideoContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), FloatingVisitorVideoContract.View {
    private var floatingVisitorVideoView: FloatingVisitorVideoView
    private val stringProvider = Dependencies.getStringProvider()
    private var mediaStateId: Int? = null
    internal var onFlipButtonClickListener: OnClickListener?
        set(value) {
            floatingVisitorVideoView.onFlipButtonClickListener = value
        }
        get() = floatingVisitorVideoView.onFlipButtonClickListener

    init {
        LayoutInflater.from(context).inflate(R.layout.visitor_video_layout_view, this)
        floatingVisitorVideoView = findViewById(R.id.visitor_video_card)
        floatingVisitorVideoView.contentDescription = stringProvider.getRemoteString(R.string.call_visitor_video_accessibility_label)
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

    override fun showFlipCameraButton(flipButtonState: FloatingVisitorVideoView.FlipButtonState) {
        floatingVisitorVideoView.showFlipCameraButton(flipButtonState)
    }

    override fun hide() {
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
            ViewHelpers.OnTouchListener(
                { viewLocation },
                { newPositionX: Float, newPositionY: Float ->
                    onViewDragged(
                        newPositionX,
                        newPositionY
                    )
                }
            ) { _: View? -> }
        )
    }

    private val viewLocation: Pair<Int?, Int?>
        get() = Pair(
            java.lang.Float.valueOf(floatingVisitorVideoView.x).toInt(),
            java.lang.Float.valueOf(floatingVisitorVideoView.y).toInt()
        )

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
