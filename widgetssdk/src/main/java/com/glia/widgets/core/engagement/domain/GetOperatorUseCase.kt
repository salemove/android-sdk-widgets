package com.glia.widgets.core.engagement.domain

import com.glia.widgets.core.engagement.GliaOperatorRepository
import com.glia.widgets.core.engagement.data.LocalOperator
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.util.Optional

internal class GetOperatorUseCase(private val gliaOperatorRepository: GliaOperatorRepository) {
    operator fun invoke(operatorId: String): Single<Optional<LocalOperator>> {
        return Single.create{ emitter ->
            gliaOperatorRepository.getOperatorById(operatorId) {
                emitter.onSuccess(Optional.ofNullable(it))
            }
        }
    }
}
