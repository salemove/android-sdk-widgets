package com.glia.widgets.snapshotutils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.util.Consumer
import com.glia.widgets.R
import com.glia.widgets.helper.load
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.mockito.Mockito.mock

interface SnapshotCoil : SnapshotTestLifecycle {

    fun mockCoil(
        @DrawableRes imageResources: List<Int> = listOf(R.drawable.test_banner),
        imageLoadError: Boolean = false
    ) {

        var index = 0

        mockkStatic("com.glia.widgets.helper.ViewExtensionsKt")
        every {
            any<ImageView>().load(
                any<String>(),
                any<Consumer<Unit>>(),
                any<Consumer<Throwable>>()
            )
        } answers {
            if (imageLoadError) {
                lastArg<Consumer<Throwable>>().accept(mock())
            } else {
                thirdArg<Consumer<Unit>>().accept(mock())
                firstArg<ImageView>().setImageResource(imageResources[index])
            }
            index += 1
        }

        every { any<ImageView>().load(any<String>()) } answers {
            firstArg<ImageView>().setImageResource(imageResources[index])
            index += 1
        }

        setOnEndListener {
            unmockkStatic("com.glia.widgets.helper.ViewExtensionsKt")
        }
    }
}
