package com.glia.widgets.core.engagement;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;
import androidx.core.util.Consumer;

import com.glia.androidsdk.Operator;
import com.glia.widgets.di.GliaCore;

public class GliaOperatorRepository {
    private final GliaCore gliaCore;

    private final SimpleArrayMap<String, Operator> cachedOperators = new SimpleArrayMap<>();

    public GliaOperatorRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void getOperatorById(@NonNull String operatorId, @NonNull Consumer<Operator> callback) {
        Operator cachedOperator = cachedOperators.get(operatorId);
        if (cachedOperator != null) {
            callback.accept(cachedOperator);
            return;
        }

        gliaCore.getOperator(operatorId, (operator, error) -> {
            if (operator != null) {
                addOrUpdateOperator(operator);
            }
            callback.accept(operator);
        });
    }

    public void addOrUpdateOperator(@NonNull Operator operator) {
        cachedOperators.put(operator.getId(), operator);
    }

}
