package com.glia.widgets.core.engagement.domain;

import androidx.annotation.NonNull;

import com.glia.androidsdk.Operator;
import com.glia.widgets.core.engagement.GliaOperatorRepository;

import java.util.Optional;

import io.reactivex.Single;

public class GetOperatorUseCase {
    private final GliaOperatorRepository gliaOperatorRepository;

    public GetOperatorUseCase(GliaOperatorRepository gliaOperatorRepository) {
        this.gliaOperatorRepository = gliaOperatorRepository;
    }

    public Single<Optional<Operator>> execute(@NonNull String operatorId) {
        return Single.create(emitter ->
                gliaOperatorRepository.getOperatorById(operatorId, operator ->
                        emitter.onSuccess(Optional.ofNullable(operator))
                )
        );
    }

}
