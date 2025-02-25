package com.glia.widgets.core.engagement

import androidx.annotation.VisibleForTesting
import androidx.collection.SimpleArrayMap
import androidx.core.util.Consumer
import com.glia.androidsdk.Operator
import com.glia.widgets.core.engagement.data.LocalOperator
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.imageUrl

internal interface GliaOperatorRepository {
    fun getOperatorById(operatorId: String, callback: Consumer<LocalOperator?>)
    fun emit(operator: Operator)
    fun updateOperatorDefaultImageUrl(imageUrl: String?)
    fun setIsAlwaysUseDefaultOperatorPicture(isAlwaysUseDefaultOperatorPicture: Boolean?)
}

internal class GliaOperatorRepositoryImpl(private val gliaCore: GliaCore) : GliaOperatorRepository {
    private val cachedOperators = SimpleArrayMap<String, LocalOperator>()

    @VisibleForTesting
    var operatorDefaultImageUrl: String? = null

    @VisibleForTesting
    var isAlwaysUseDefaultOperatorPicture: Boolean = false

    override fun getOperatorById(operatorId: String, callback: Consumer<LocalOperator?>) {
        val cachedOperator: LocalOperator?
        synchronized(cachedOperators) {
            cachedOperator = cachedOperators[operatorId]
        }
        if (cachedOperator != null) {
            callback.accept(cachedOperator)
            return
        }
        gliaCore.getOperator(operatorId) { operator: Operator?, _ ->
            operator?.let { mapOperator(it) }?.also { putOperator(it) }.also { callback.accept(it) }
        }
    }

    override fun emit(operator: Operator) = putOperator(mapOperator(operator))

    @VisibleForTesting
    fun mapOperator(operator: Operator): LocalOperator = operator.run { LocalOperator(id, name, getOperatorImage(operator.imageUrl)) }

    @VisibleForTesting
    fun getOperatorImage(imageUrl: String?): String? = if (isAlwaysUseDefaultOperatorPicture || imageUrl == null) {
        operatorDefaultImageUrl
    } else {
        imageUrl
    }

    @VisibleForTesting
    fun putOperator(operator: LocalOperator) {
        synchronized(cachedOperators) {
            operator.apply { cachedOperators.put(id, this) }
        }
    }

    override fun updateOperatorDefaultImageUrl(imageUrl: String?) {
        operatorDefaultImageUrl = imageUrl
    }

    override fun setIsAlwaysUseDefaultOperatorPicture(isAlwaysUseDefaultOperatorPicture: Boolean?) {
        this.isAlwaysUseDefaultOperatorPicture = isAlwaysUseDefaultOperatorPicture ?: false
    }
}
