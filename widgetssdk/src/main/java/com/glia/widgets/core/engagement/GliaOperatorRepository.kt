package com.glia.widgets.core.engagement

import androidx.annotation.VisibleForTesting
import androidx.collection.SimpleArrayMap
import androidx.core.util.Consumer
import com.glia.androidsdk.Operator
import com.glia.widgets.core.engagement.data.LocalOperator
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.imageUrl

internal class GliaOperatorRepository(private val gliaCore: GliaCore) {
    private val cachedOperators = SimpleArrayMap<String, LocalOperator>()

    @VisibleForTesting
    var operatorDefaultImageUrl: String? = null

    @VisibleForTesting
    var isAlwaysUseDefaultOperatorPicture: Boolean = false

    fun getOperatorById(operatorId: String, callback: Consumer<LocalOperator?>) {
        val cachedOperator = cachedOperators[operatorId]
        if (cachedOperator != null) {
            callback.accept(cachedOperator)
            return
        }
        gliaCore.getOperator(operatorId) { operator: Operator?, _ ->
            operator?.let { mapOperator(it) }?.also { putOperator(it) }.also { callback.accept(it) }
        }
    }

    fun emit(operator: Operator) = putOperator(mapOperator(operator))

    @VisibleForTesting
    fun mapOperator(operator: Operator): LocalOperator = operator.run { LocalOperator(id, name, getOperatorImage(operator.imageUrl)) }

    @VisibleForTesting
    fun getOperatorImage(imageUrl: String?): String? {
        return if (isAlwaysUseDefaultOperatorPicture || imageUrl == null) operatorDefaultImageUrl
        else imageUrl
    }

    @VisibleForTesting
    fun putOperator(operator: LocalOperator) {
        operator.apply { cachedOperators.put(id, this) }
    }

    fun updateOperatorDefaultImageUrl(imageUrl: String?) {
        operatorDefaultImageUrl = imageUrl
    }

    fun setIsAlwaysUseDefaultOperatorPicture(isAlwaysUseDefaultOperatorPicture: Boolean?) {
        this.isAlwaysUseDefaultOperatorPicture = isAlwaysUseDefaultOperatorPicture ?: false
    }
}
