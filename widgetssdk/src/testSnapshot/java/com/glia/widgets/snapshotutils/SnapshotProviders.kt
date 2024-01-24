package com.glia.widgets.snapshotutils

import com.glia.widgets.StringProvider
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider

interface SnapshotProviders: SnapshotContent, SnapshotTestLifecycle {

    fun stringProviderMock(): StringProvider {
        val stringProvider: StringProvider = SnapshotStringProvider(context)
        Dependencies.setStringProvider(stringProvider)

        setOnEndListener {
            Dependencies.setStringProvider(null)
        }

        return stringProvider
    }

    fun resourceProviderMock(): ResourceProvider {
        val resourceProvider = ResourceProvider(context)
        Dependencies.setResourceProvider(resourceProvider)

        setOnEndListener {
            Dependencies.setResourceProvider(null)
        }

        return resourceProvider
    }
}
