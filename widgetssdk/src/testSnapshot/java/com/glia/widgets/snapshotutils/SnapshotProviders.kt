package com.glia.widgets.snapshotutils

import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair

internal interface SnapshotProviders: SnapshotContent, SnapshotTestLifecycle {

    val snapshotLocales: Map<Int, String>

    fun localeProviderMock(): LocaleProvider {
        val resourceProvider = resourceProviderMock()
        val localeProvider = object: LocaleProvider(resourceProvider) {
            override fun getStringInternal(stringKey: Int, values: List<StringKeyPair>): String {
                return snapshotLocales[stringKey] ?: context.resources.getResourceName(stringKey).split("/")[1]
            }
        }

        Dependencies.localeProvider = localeProvider

        return localeProvider
    }

    fun resourceProviderMock(): ResourceProvider {
        val resourceProvider = ResourceProvider(context)
        Dependencies.resourceProvider = resourceProvider

//        setOnEndListener {
//            Dependencies.resourceProvider = null
//        }

        return resourceProvider
    }
}
