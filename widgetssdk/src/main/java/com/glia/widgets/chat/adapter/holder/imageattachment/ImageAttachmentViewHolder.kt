package com.glia.widgets.chat.adapter.holder.imageattachment

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.Logger.e
import com.glia.widgets.helper.fileName
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.google.android.material.imageview.ShapeableImageView
import io.reactivex.rxjava3.disposables.Disposable

internal open class ImageAttachmentViewHolder(
    itemView: View,
    private val imageView: ShapeableImageView,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    private val schedulers: Schedulers
) : RecyclerView.ViewHolder(itemView) {
    private var disposable: Disposable? = null

    fun bind(attachmentFile: AttachmentFile) {
        imageView.setImageResource(android.R.color.darker_gray) // clear the previous view state
        val imageName = attachmentFile.fileName
        disposable = getImageFileFromCacheUseCase.invoke(imageName)
            .doOnError { error: Throwable -> d(TAG, "failed loading from cache: " + imageName + " reason: " + error.message) }
            .doOnSuccess { _: Bitmap? -> d(TAG, "loaded from cache: $imageName") }
            .onErrorResumeNext { getImageFileFromDownloadsUseCase.invoke(imageName) }
            .doOnError { error: Throwable -> d(TAG, imageName + " failed loading from downloads: " + error.message) }
            .doOnSuccess { _: Bitmap? -> d(TAG, "loaded from downloads: $imageName") }
            .onErrorResumeNext { getImageFileFromNetworkUseCase.invoke(attachmentFile) }
            .doOnError { error: Throwable -> d(TAG, imageName + " failed loading from network: " + error.message) }
            .doOnSuccess { _: Bitmap? -> d(TAG, "loaded from network: $imageName") }
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
            .subscribe({ bm: Bitmap? -> imageView.setImageBitmap(bm) }
            ) { error: Throwable ->
                error.message?.let { e(TAG, it) }
                imageView.setBackgroundColor(Color.BLACK)
            }
        setAccessibilityActions()
    }

    fun bind(localAttachment: LocalAttachment) {
        imageView.load(localAttachment.uri)
    }

    private fun setAccessibilityActions() {
        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val actionLabel =
                    host.resources.getString(R.string.glia_chat_attachment_open_button_label)
                val actionClick = AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                )
                info.addAction(actionClick)
            }
        })
    }

    fun onStopView() {
        if (disposable != null) disposable!!.dispose()
    }

    companion object {
        private val TAG = ImageAttachmentViewHolder::class.java.simpleName
    }
}
