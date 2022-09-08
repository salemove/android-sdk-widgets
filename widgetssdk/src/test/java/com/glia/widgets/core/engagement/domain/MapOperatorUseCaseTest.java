package com.glia.widgets.core.engagement.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.OperatorMessage;
import com.glia.widgets.core.model.TestOperator;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import io.reactivex.Single;

public class MapOperatorUseCaseTest {
    MapOperatorUseCase mapOperatorUseCase;
    GetOperatorUseCase getOperatorUseCase;
    Operator operator;

    @Before
    public void setUp() throws Exception {
        getOperatorUseCase = mock(GetOperatorUseCase.class);
        mapOperatorUseCase = new MapOperatorUseCase(getOperatorUseCase);
        operator = TestOperator.DEFAULT;
    }

    @Test
    public void execute_returnsInternalMessageWithoutOperator_whenChatMessageIsVisitor() {
        ChatMessage chatMessage = mock(ChatMessage.class);
        when(chatMessage.isOperator()).thenReturn(false);

        mapOperatorUseCase.execute(chatMessage)
                .test()
                .assertComplete()
                .assertNever(messageInternal -> messageInternal.getOperator().isPresent());
    }

    @Test
    public void execute_returnsInternalMessageWithOperator_whenChatMessageIsOperator() {
        OperatorMessage chatMessage = mock(OperatorMessage.class);
        when(chatMessage.isOperator()).thenReturn(true);
        when(chatMessage.getOperatorId()).thenReturn(operator.getId());

        when(getOperatorUseCase.execute(operator.getId())).thenReturn(Single.just(Optional.of(operator)));

        mapOperatorUseCase.execute(chatMessage)
                .doOnSuccess(messageInternal -> assertEquals(messageInternal.getOperatorId().get(), operator.getId()))
                .test()
                .assertComplete()
                .assertNever(messageInternal -> !messageInternal.getOperator().isPresent());
    }
}