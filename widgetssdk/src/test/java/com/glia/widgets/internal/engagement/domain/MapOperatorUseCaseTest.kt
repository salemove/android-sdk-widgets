package com.glia.widgets.internal.engagement.domain

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.internal.engagement.data.LocalOperator
import io.reactivex.rxjava3.core.Single
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional
import kotlin.properties.Delegates

class MapOperatorUseCaseTest {
    private var mapOperatorUseCase: MapOperatorUseCase by Delegates.notNull()
    private var getOperatorUseCase: GetOperatorUseCase by Delegates.notNull()
    private var operator: LocalOperator by Delegates.notNull()

    @Before
    fun setUp() {
        getOperatorUseCase = mock()
        mapOperatorUseCase = MapOperatorUseCase(getOperatorUseCase)
        operator = LocalOperator("id", "name", "imageUrl")
    }

    @Test
    fun execute_returnsInternalMessageWithoutOperator_whenChatMessageIsVisitor() {
        val chatMessage: ChatMessage = mock()
        whenever(chatMessage.senderType) doReturn Chat.Participant.SYSTEM
        mapOperatorUseCase(chatMessage)
            .test()
            .assertComplete()
            .assertValue { it.operator == null }
    }

    @Test
    fun execute_returnsInternalMessageWithOperator_whenChatMessageIsOperator() {
        val chatMessage: OperatorMessage = mock()
        whenever(chatMessage.senderType) doReturn Chat.Participant.OPERATOR
        whenever(chatMessage.operatorId) doReturn operator.id
        whenever(getOperatorUseCase(operator.id)) doReturn (Single.just(Optional.of(operator)))
        mapOperatorUseCase(chatMessage)
            .doOnSuccess { assertEquals(it.operatorId, operator.id) }
            .test()
            .assertComplete()
            .assertValue { it.operator != null }
    }
}
