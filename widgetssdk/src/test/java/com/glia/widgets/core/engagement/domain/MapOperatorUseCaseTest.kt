package com.glia.widgets.core.engagement.domain

import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.model.TestOperator
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional
import kotlin.properties.Delegates

class MapOperatorUseCaseTest {
    private var mapOperatorUseCase: MapOperatorUseCase by Delegates.notNull()
    var getOperatorUseCase: GetOperatorUseCase by Delegates.notNull()
    var operator: Operator by Delegates.notNull()

    @Before
    fun setUp() {
        getOperatorUseCase = mock()
        mapOperatorUseCase = MapOperatorUseCase(getOperatorUseCase)
        operator = TestOperator.DEFAULT
    }

    @Test
    fun execute_returnsInternalMessageWithoutOperator_whenChatMessageIsVisitor() {
        val chatMessage: ChatMessage = mock()
        whenever(chatMessage.senderType) doReturn Chat.Participant.SYSTEM
        mapOperatorUseCase(chatMessage)
            .test()
            .assertComplete()
            .assertNever { it.operator.isPresent }
    }

    @Test
    fun execute_returnsInternalMessageWithOperator_whenChatMessageIsOperator() {
        val chatMessage: OperatorMessage = mock()
        whenever(chatMessage.senderType) doReturn Chat.Participant.OPERATOR
        whenever(chatMessage.operatorId) doReturn operator.id
        whenever(getOperatorUseCase.execute(operator.id)) doReturn (Single.just(Optional.of(operator)))
        mapOperatorUseCase(chatMessage)
            .doOnSuccess { assertEquals(it.operatorId.get(), operator.id) }
            .test()
            .assertComplete()
            .assertNever { !it.operator.isPresent }
    }
}