package com.glia.widgets.snapshotutils

import android.net.Uri
import android.widget.ImageView
import com.glia.widgets.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

interface SnapshotPicasso: SnapshotTestLifecycle {

    data class Mock(
        val mockStatic: MockedStatic<Picasso>,
        val picasso: Picasso,
        val requestCreator: RequestCreator
    )

    fun picassoMock(imageResources: List<Int> = listOf(R.drawable.test_banner)): Mock {
        val picassoMock = mock<Picasso>()
        val mockStatic = Mockito.mockStatic(Picasso::class.java)
        mockStatic.`when`<Any> { Picasso.get() }.thenReturn(picassoMock)
        val requestCreatorMock = mock<RequestCreator>()
        whenever(picassoMock.load(any<Uri>())).thenReturn(requestCreatorMock)
        whenever(requestCreatorMock.resize(any(), any())).thenReturn(requestCreatorMock)
        whenever(requestCreatorMock.onlyScaleDown()).thenReturn(requestCreatorMock)

        var index = 0
        whenever(requestCreatorMock.into(any<ImageView>())).thenAnswer {
            fillImages(it.getArgument(0), imageResources, index)
            index += 1
            return@thenAnswer Unit
        }

        setOnEndListener {
            mockStatic.close()
        }

        return Mock(mockStatic, picassoMock, requestCreatorMock)
    }

    private fun fillImages(
        imageView: ImageView,
        imageResources: List<Int>,
        index: Int
    ) {
        val imageIndex = index % imageResources.size
        imageView.setImageResource(imageResources[imageIndex])
    }
}
