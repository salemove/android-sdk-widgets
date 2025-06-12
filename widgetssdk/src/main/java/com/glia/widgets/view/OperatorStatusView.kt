package com.glia.widgets.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import coil3.dispose
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.OperatorStatusViewBinding
import com.glia.widgets.helper.addColorFilter
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.load
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.chat.OnHoldOverlayTheme
import com.glia.widgets.view.unifiedui.theme.chat.OperatorTheme
import com.glia.widgets.view.unifiedui.theme.chat.UserImageTheme
import com.google.android.material.imageview.ShapeableImageView
import kotlin.properties.Delegates

internal class OperatorStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy {
        OperatorStatusViewBinding.bind(inflate(context, R.layout.operator_status_view, this))
    }

    // Main view
    private val profilePictureView: ShapeableImageView
        get() = binding.profilePictureView

    // Icon on top of main view - displayed if no image available
    private val placeholderView: ShapeableImageView
        get() = binding.placeholderView

    private val profilePictureBackgroundColorDrawable by lazy { GradientDrawable() }

    // On Hold status view on top of main view - displayed when on hold status changes
    private val onHoldOverlayView: ShapeableImageView
        get() = binding.onHoldIcon

    private var operatorImageSize: Int by Delegates.notNull()
    private var operatorImageContentPadding: Int by Delegates.notNull()

    private val operatorImageLargeSize by lazy {
        resources.getDimensionPixelSize(R.dimen.glia_chat_profile_picture_large_size)
    }
    private val operatorImageLargeContentPadding by lazy {
        resources.getDimensionPixelSize(R.dimen.glia_chat_profile_picture_large_content_padding)
    }
    private var isOnHold = false

    init {
        context.withStyledAttributes(attrs, R.styleable.OperatorStatusView) {
            val rippleAnimationColor = getResourceId(R.styleable.OperatorStatusView_rippleTint, 0)

            if (rippleAnimationColor != 0) {
                binding.rippleAnimation.addColorFilter(getColorCompat(rippleAnimationColor))
            }

            operatorImageSize = getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageSize,
                resources.getDimensionPixelSize(R.dimen.glia_chat_profile_picture_size)
            )

            operatorImageContentPadding = getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageContentPadding,
                resources.getDimensionPixelSize(R.dimen.glia_chat_profile_picture_content_padding)
            )

            updateProfilePictureViewSize(operatorImageSize)
        }

        setOnHoldVisibility()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        profilePictureView.dispose()
    }

    fun setTheme(theme: UiTheme) {
        // icons
        setPlaceHolderIcon(theme)
        setOnHoldIcon(theme)

        // colors
        theme.brandPrimaryColor?.let(::getColorCompat)
            ?.also(profilePictureBackgroundColorDrawable::setColor)
        theme.brandPrimaryColor?.let(::getColorCompat)
            ?.also(binding.rippleAnimation::addColorFilter)

        profilePictureView.setLocaleContentDescription(R.string.call_operator_avatar_accessibility_label)
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable)
        theme.baseLightColor?.let(::getColorStateListCompat)
            ?.also(placeholderView::setImageTintList)
    }

    internal fun applyUserImageTheme(userImageTheme: UserImageTheme?) {
        if (userImageTheme == null) return

        userImageTheme.placeholderBackgroundColor?.also(::applyPlaceholderBackgroundColorTheme)

        userImageTheme.imageBackgroundColor?.also {
            profilePictureView.backgroundTintList = it.primaryColorStateList
        }

        userImageTheme.placeholderColor?.also {
            placeholderView.imageTintList = it.primaryColorStateList
        }
    }

    internal fun applyPlaceholderBackgroundColorTheme(colorTheme: ColorTheme?) {
        colorTheme?.also {
            if (it.isGradient) {
                profilePictureBackgroundColorDrawable.colors = it.valuesArray
            } else {
                profilePictureBackgroundColorDrawable.setColor(it.primaryColor)
            }
        }
    }

    internal fun applyRippleColorTheme(colorTheme: ColorTheme?) {
        colorTheme?.let(ColorTheme::primaryColor)?.also(binding.rippleAnimation::addColorFilter)
    }

    internal fun applyOperatorTheme(operatorTheme: OperatorTheme?) {
        applyUserImageTheme(operatorTheme?.image)
        applyRippleColorTheme(operatorTheme?.animationColor)
        applyOnHoldOverlayTheme(operatorTheme?.onHoldOverlay)
    }

    internal fun applyOnHoldOverlayTheme(onHoldOverlayTheme: OnHoldOverlayTheme?) {
        onHoldOverlayView.setLocaleContentDescription(R.string.android_call_on_hold_icon_accessibility)
        onHoldOverlayView.applyImageColorTheme(onHoldOverlayTheme?.tintColor)
        onHoldOverlayTheme?.backgroundColor?.primaryColorStateList?.also(onHoldOverlayView::setBackgroundTintList)
    }

    fun showTransferring() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable)
        updateProfilePictureViewSize(operatorImageSize)
        updatePlaceholderView(operatorImageSize, operatorImageContentPadding, VISIBLE)
    }

    fun showPlaceHolderWithIconPaddingOnConnect() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable)
        updateProfilePictureViewSize(operatorImageLargeSize)
        updatePlaceholderView(operatorImageLargeSize, operatorImageLargeContentPadding, VISIBLE)
    }

    fun showProfileImageOnConnect(profileImgUrl: String?) {
        updateProfilePictureViewSize(operatorImageLargeSize)
        profilePictureView.load(
            profileImgUrl,
            onSuccess = {
                updatePlaceholderView(operatorImageLargeSize, 0, GONE)
            }, onError = {
                showPlaceholder()
            })
    }

    fun showProfileImage(profileImgUrl: String?) {
        updateProfilePictureViewSize(operatorImageSize)
        profilePictureView.load(
            profileImgUrl,
            onSuccess = {
                updatePlaceholderView(operatorImageSize, 0, GONE)
            }, onError = {
                showPlaceholder()
            })
    }

    fun showPlaceholder() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable)
        updateProfilePictureViewSize(operatorImageSize)
        updatePlaceholderView(operatorImageSize, operatorImageContentPadding, VISIBLE)
    }

    fun setShowOnHold(isOnHold: Boolean) {
        if (this.isOnHold != isOnHold) {
            this.isOnHold = isOnHold
            setOnHoldVisibility()
        }
    }

    fun setShowRippleAnimation(show: Boolean) {
        if (show) {
            showRippleAnimation()
        } else {
            hideRippleAnimation()
        }
    }

    private fun updatePlaceholderView(size: Int, contentPadding: Int, visibility: Int) {
        placeholderView.visibility = visibility
        placeholderView.layoutParams.width = size
        placeholderView.layoutParams.height = size
        setPlaceholderViewContentPadding(contentPadding)
    }

    private fun setPlaceholderViewContentPadding(contentPadding: Int) {
        placeholderView.setPaddingRelative(
            contentPadding,
            contentPadding,
            contentPadding,
            contentPadding
        )
    }

    private fun updateProfilePictureViewSize(size: Int) {
        profilePictureView.layoutParams.width = size
        profilePictureView.layoutParams.height = size
    }

    private fun showRippleAnimation() {
        binding.rippleAnimation.playAnimation()
        binding.rippleAnimation.isVisible = true
    }

    private fun hideRippleAnimation() {
        binding.rippleAnimation.cancelAnimation()
        binding.rippleAnimation.isGone = true
    }

    private fun setOnHoldVisibility() {
        onHoldOverlayView.isVisible = isOnHold
    }

    private fun setPlaceHolderIcon(theme: UiTheme) {
        theme.iconPlaceholder?.also(placeholderView::setImageResource)
    }

    private fun setOnHoldIcon(theme: UiTheme) {
        theme.iconOnHold?.also(onHoldOverlayView::setImageResource)
    }
}
