package com.glia.widgets.snapshotutils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.glia.widgets.helper.load
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

interface SnapshotCoil: SnapshotTestLifecycle {

//    fun mockCoil(
//        @DrawableRes imageResources: List<Int> = listOf(R.drawable.test_banner),
//        imageLoadError: Boolean
//    ) {
//        mockCoil { _, callback ->
//            if (imageLoadError) {
//                callback.onError(Exception())

//            } else {
//                callback.onSuccess()
//            }
//        }
//    }

    fun mockCoil(
        @DrawableRes imageResources: List<Int> = listOf(R.drawable.test_banner),
//        loadCallback: ((Int, Callback) -> Unit)? = null
//        imageLoadError: Boolean
    ) {
        mockkStatic("com.glia.widgets.helper.ViewExtensionsKt")
//        mockkStatic("android.widget.ImageView")

        var index = 0
        every { any<ImageView>().load(any(), null, null) } answers {
            firstArg<ImageView>().setImageResource(R.drawable.test_banner)
        }
        index += 1
        setOnEndListener {
            unmockkStatic("com.glia.widgets.helper.ViewExtensionsKt")
        }
    }

    @DrawableRes
    private fun getImageResource(
        imageResources: List<Int>,
        index: Int
    ): Int {
        val imageIndex = index % imageResources.size
        return imageResources[imageIndex]
    }
}
