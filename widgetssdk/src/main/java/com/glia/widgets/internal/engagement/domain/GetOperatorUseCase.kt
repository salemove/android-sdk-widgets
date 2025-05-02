package com.glia.widgets.internal.engagement.domain

import com.glia.widgets.internal.engagement.GliaOperatorRepository
import com.glia.widgets.internal.engagement.data.LocalOperator
import io.reactivex.rxjava3.core.Single
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
