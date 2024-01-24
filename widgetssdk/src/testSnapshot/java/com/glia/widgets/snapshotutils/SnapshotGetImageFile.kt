package com.glia.widgets.snapshotutils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.UseCaseFactory
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import io.reactivex.Maybe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotGetImageFile : SnapshotTestLifecycle, SnapshotContent {
    data class Mock(
        val useCaseFactoryMock: UseCaseFactory,
        val getImageFileFromCacheUseCaseMock: GetImageFileFromCacheUseCase,
        val getImageFileFromDownloadsUseCaseMock: GetImageFileFromDownloadsUseCase,
        val getImageFileFromNetworkUseCaseMock: GetImageFileFromNetworkUseCase,
    )

    fun getImageFileMock(@DrawableRes imageRes: Int? = null) = getImageFileMock(
        imageRes?.let { BitmapFactory.decodeResource(resources, it) }
    )

    fun getImageFileMock(bitmap: Bitmap?): Mock {
        val getImageFileFromCacheUseCaseMock = mock<GetImageFileFromCacheUseCase>()
        whenever(getImageFileFromCacheUseCaseMock.invoke(any())) doReturn result(bitmap)
        val getImageFileFromDownloadsUseCaseMock = mock<GetImageFileFromDownloadsUseCase>()
        whenever(getImageFileFromDownloadsUseCaseMock.invoke(any())) doReturn result(bitmap)
        val getImageFileFromNetworkUseCaseMock = mock<GetImageFileFromNetworkUseCase>()
        whenever(getImageFileFromNetworkUseCaseMock.invoke(any())) doReturn result(bitmap)
        val useCaseFactoryMock = mock<UseCaseFactory>()
        whenever(useCaseFactoryMock.createGetImageFileFromCacheUseCase()).thenReturn(getImageFileFromCacheUseCaseMock)
        whenever(useCaseFactoryMock.createGetImageFileFromDownloadsUseCase()).thenReturn(getImageFileFromDownloadsUseCaseMock)
        whenever(useCaseFactoryMock.createGetImageFileFromNetworkUseCase()).thenReturn(getImageFileFromNetworkUseCaseMock)

        Dependencies.setUseCaseFactory(useCaseFactoryMock)

        setOnEndListener {
            Dependencies.setUseCaseFactory(null)
        }

        return Mock(useCaseFactoryMock, getImageFileFromCacheUseCaseMock, getImageFileFromDownloadsUseCaseMock, getImageFileFromNetworkUseCaseMock)
    }

    private fun result(bitmap: Bitmap?) = bitmap?.let { Maybe.just(it) } ?: run { Maybe.empty() }
}
