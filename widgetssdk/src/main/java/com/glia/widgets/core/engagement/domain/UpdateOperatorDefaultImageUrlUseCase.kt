package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.core.engagement.GliaOperatorRepository
import kotlin.jvm.optionals.getOrNull

internal class UpdateOperatorDefaultImageUrlUseCase(
    private val operatorRepository: GliaOperatorRepository,
    private val siteInfoUseCase: SiteInfoUseCase
) {
    operator fun invoke() = siteInfoUseCase.execute { response, _ ->
        response.defaultOperatorPicture?.url?.getOrNull()?.also(operatorRepository::updateOperatorDefaultImageUrl)
    }
}
