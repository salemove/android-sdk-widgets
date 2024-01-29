package com.glia.widgets.snapshotutils

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.glia.widgets.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.Exception

interface SnapshotPicasso: SnapshotTestLifecycle {

    data class Mock(
        val mockStatic: MockedStatic<Picasso>,
        val picasso: Picasso,
        val requestCreator: RequestCreator
    )

    fun picassoMock(
        @DrawableRes imageResources: List<Int> = listOf(R.drawable.test_banner),
        imageLoadError: Boolean
    ) {
        picassoMock { _, callback ->
            if (imageLoadError) {
                callback.onError(Exception())
            } else {
                callback.onSuccess()
            }
        }
    }

    fun picassoMock(
        @DrawableRes imageResources: List<Int> = listOf(R.drawable.test_banner),
        loadCallback: ((Int, Callback) -> Unit)? = null
    ): Mock {
        val picassoMock = mock<Picasso>()
        val mockStatic = Mockito.mockStatic(Picasso::class.java)
        mockStatic.`when`<Any> { Picasso.get() }.thenReturn(picassoMock)
        val requestCreatorMock = mock<RequestCreator>()
        whenever(picassoMock.load(any<Uri>())).thenReturn(requestCreatorMock)
        whenever(picassoMock.load(any<String>())).thenReturn(requestCreatorMock)
        whenever(requestCreatorMock.resize(any(), any())).thenReturn(requestCreatorMock)
        whenever(requestCreatorMock.onlyScaleDown()).thenReturn(requestCreatorMock)

        var index = 0
        val loadAnswer: (invocation: InvocationOnMock) -> Unit = { invocation ->
            val imageView = invocation.getArgument<ImageView>(0)
            val imageResource = getImageResource(imageResources, index)
            invocation.arguments.getOrNull(1)?.let { it as Callback }?.let { callback ->
                loadCallback?.invoke(imageResource, object : Callback {
                    override fun onSuccess() {
                        callback.onSuccess()
                        imageView.setImageResource(imageResource)
                    }

                    override fun onError(e: Exception) {
                        callback.onError(e)
                    }
                }) ?: run {
                    callback.onSuccess()
                    imageView.setImageResource(imageResource)
                }
            } ?: run {
                imageView.setImageResource(imageResource)
            }

            index += 1
        }

        whenever(requestCreatorMock.into(any<ImageView>())).thenAnswer(loadAnswer)
        whenever(requestCreatorMock.into(any(), anyOrNull())).thenAnswer(loadAnswer)

        setOnEndListener {
            mockStatic.close()
        }

        return Mock(mockStatic, picassoMock, requestCreatorMock)
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
