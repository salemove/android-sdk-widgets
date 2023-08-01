package com.glia.widgets.core.engagement

import androidx.annotation.VisibleForTesting
import androidx.collection.SimpleArrayMap
import androidx.core.util.Consumer
import com.glia.androidsdk.Operator
import com.glia.widgets.core.engagement.data.LocalOperator
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.toLocal

internal class GliaOperatorRepository(private val gliaCore: GliaCore) {
    private val cachedOperators = SimpleArrayMap<String, LocalOperator>()
    fun getOperatorById(operatorId: String, callback: Consumer<LocalOperator?>) {
        val cachedOperator = cachedOperators[operatorId]
        if (cachedOperator != null) {
            callback.accept(cachedOperator)
            return
        }
        gliaCore.getOperator(operatorId) { operator: Operator?, _ ->
            operator?.let { updateIfExists(it) }?.also { putOperator(it) }.also { callback.accept(it) }
        }
    }

    fun emit(operator: Operator) = putOperator(updateIfExists(operator))

    @VisibleForTesting
    fun updateIfExists(operator: Operator): LocalOperator {
        val newOperator = operator.toLocal()

        val oldOperator = cachedOperators[operator.id] ?: return newOperator

        return if (oldOperator.imageUrl != null) oldOperator else oldOperator.copy(imageUrl = newOperator.imageUrl)
    }

    @VisibleForTesting
    fun putOperator(operator: LocalOperator) {
        operator.apply { cachedOperators.put(id, this) }
    }

}
