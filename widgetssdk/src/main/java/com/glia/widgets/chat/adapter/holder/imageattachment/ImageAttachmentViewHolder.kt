package com.glia.widgets.chat.adapter.holder.imageattachment

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.chat.model.Attachment
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.Logger.e
import com.glia.widgets.helper.fileName
import com.glia.widgets.helper.rx.Schedulers
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import io.reactivex.rxjava3.disposables.Disposable

internal open class ImageAttachmentViewHolder(
    itemView: View,
    private val getImageFileFromCacheUseCase: GetImageFileFromCacheUseCase,
    private val getImageFileFromDownloadsUseCase: GetImageFileFromDownloadsUseCase,
    private val getImageFileFromNetworkUseCase: GetImageFileFromNetworkUseCase,
    private val schedulers: Schedulers
) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ShapeableImageView
    private var disposable: Disposable? = null

    init {
        imageView = itemView.findViewById(R.id.incoming_image_attachment)
    }

    fun bind(attachment: Attachment) {
        val attachmentFile = attachment.remoteAttachment
        attachmentFile?.let { bind(it) }
        val fileAttachment = attachment.localAttachment
        fileAttachment?.let { bind(it) }
    }

    private fun bind(attachmentFile: AttachmentFile) {
        imageView.setImageResource(android.R.color.transparent) // clear the previous view state
        val imageName = attachmentFile.fileName
        disposable = getImageFileFromCacheUseCase.invoke(imageName)
            .doOnError { error: Throwable -> d(TAG, "failed loading from cache: " + imageName + " reason: " + error.message) }
            .doOnSuccess { _: Bitmap? -> d(TAG, "loaded from cache: $imageName") }
            .onErrorResumeNext { getImageFileFromDownloadsUseCase.invoke(imageName) }
            .doOnError { error: Throwable -> d(TAG, imageName + " failed loading from downloads: " + error.message) }
            .doOnSuccess { _: Bitmap? -> d(TAG, "loaded from downloads: $imageName") }
            .onErrorResumeNext { getImageFileFromNetworkUseCase.invoke(attachmentFile)}
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

    private fun bind(fileAttachment: FileAttachment) {
        Picasso.get()
            .load(fileAttachment.uri)
            .into(imageView)
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
