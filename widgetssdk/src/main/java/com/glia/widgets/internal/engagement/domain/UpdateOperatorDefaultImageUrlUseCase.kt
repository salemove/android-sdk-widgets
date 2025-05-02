package com.glia.widgets.internal.engagement.domain

import com.glia.widgets.chat.domain.SiteInfoUseCase
import com.glia.widgets.internal.engagement.GliaOperatorRepository
import kotlin.jvm.optionals.getOrNull

internal class UpdateOperatorDefaultImageUrlUseCase(
    private val operatorRepository: GliaOperatorRepository,
    private val siteInfoUseCase: SiteInfoUseCase
) {
    operator fun invoke() = siteInfoUseCase.invoke { response, _ ->
        operatorRepository.setIsAlwaysUseDefaultOperatorPicture(response?.isAlwaysUseDefaultOperatorPicture)
        response?.defaultOperatorPicture?.url?.getOrNull()?.also(operatorRepository::updateOperatorDefaultImageUrl)
    }
}
