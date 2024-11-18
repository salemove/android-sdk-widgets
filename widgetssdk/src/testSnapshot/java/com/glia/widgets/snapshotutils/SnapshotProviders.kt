package com.glia.widgets.snapshotutils

import android.content.Context
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import org.mockito.kotlin.spy

/**
 * The goal of this interface is to give access to test case [LocaleProvider] and [ResourceProvider] instances
 * and to inject them into [Dependencies].
 *
 * But Kotlin interfaces do not allow default values for property instances.
 * Force classes that extend this interface to declare a property that will handle all the required logic with
 * a simple constructor.
 */
internal interface SnapshotProviders: SnapshotContent, SnapshotTestLifecycle {
    var _snapshotProvider: SnapshotProviderImp

    fun resourceProviderMock() = _snapshotProvider.resourceProvider
    fun localeProviderMock() = _snapshotProvider.localeProvider

    fun providerMockReset() {
        _snapshotProvider = SnapshotProviderImp(context)
        Dependencies.resourceProvider = resourceProviderMock()
        Dependencies.localeProvider = localeProviderMock()
    }
}

internal class SnapshotProviderImp(context: Context) {
    val resourceProvider by lazy { spy(ResourceProvider(context)) }
    val localeProvider by lazy { spy(LocaleProvider(resourceProvider)) }
}
