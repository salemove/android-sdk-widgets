package com.glia.widgets.chat.domain

import com.glia.widgets.chat.adapter.CustomCardAdapter

internal class CustomCardTypeUseCase(private val adapter: CustomCardAdapter?) {
    operator fun invoke(chatAdapterViewType: Int): Int? {
        return adapter?.getCustomCardViewType(chatAdapterViewType)
    }
}
