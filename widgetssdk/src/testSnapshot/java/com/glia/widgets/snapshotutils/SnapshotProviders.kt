package com.glia.widgets.snapshotutils

import com.glia.widgets.StringProvider
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider

interface SnapshotProviders: SnapshotContent {

    fun stringProviderMock(): StringProvider {
        val stringProvider: StringProvider = SnapshotStringProvider(context)
        Dependencies.setStringProvider(stringProvider)

        return stringProvider
    }

    fun resourceProviderMock(): ResourceProvider {
        val resourceProvider = ResourceProvider(context)
        Dependencies.setResourceProvider(resourceProvider)

        return resourceProvider
    }
}
