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

interface SnapshotPicasso: SnapshotTestLifecycle {

    data class Mock(
        val mockStatic: MockedStatic<Picasso>,
        val picasso: Picasso,
        val requestCreator: RequestCreator
    )

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
            val image = fillImages(invocation.getArgument(0), imageResources, index)
            if (loadCallback != null && invocation.arguments.size > 1) {
                loadCallback(image, invocation.getArgument(1))
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
    private fun fillImages(
        imageView: ImageView,
        imageResources: List<Int>,
        index: Int
    ): Int {
        val imageIndex = index % imageResources.size
        val imageResource = imageResources[imageIndex]
        imageView.setImageResource(imageResource)
        return imageResource
    }
}
