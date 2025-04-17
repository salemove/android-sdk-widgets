package com.glia.widgets.chat.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.widgets.chat.adapter.CustomCardAdapter;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class CustomCardShouldShowUseCaseTest {
    private static final Integer VIEW_TYPE = 10;

    private CustomCardAdapter customCardAdapter;
    private CustomCardShouldShowUseCase useCase;

    @Before
    public void setUp() {
        customCardAdapter = mock(CustomCardAdapter.class);
        when(customCardAdapter.getChatAdapterViewType(any())).thenReturn(null);
        useCase = new CustomCardShouldShowUseCase(customCardAdapter);
    }

    @Test
    public void execute_returnsTrue_whenIsNotSingleChoiceAttachment() {
        ChatMessage message = mock(ChatMessage.class);
        com.glia.androidsdk.chat.MessageAttachment attachment = mock(com.glia.androidsdk.chat.MessageAttachment.class);
        when(message.getAttachment()).thenReturn(attachment);
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, true);

        assertTrue(result);
    }

    @Test
    public void execute_returnsTrue_whenAttachmentIsNull() {
        ChatMessage message = mock(ChatMessage.class);
        com.glia.androidsdk.chat.MessageAttachment attachment = mock(com.glia.androidsdk.chat.MessageAttachment.class);
        when(message.getAttachment()).thenReturn(attachment);
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, true);

        assertTrue(result);
    }

    @Test
    public void execute_returnsTrue_whenSelectedOptionIsNull() {
        ChatMessage message = mock(ChatMessage.class);
        SingleChoiceAttachment attachment = mock(SingleChoiceAttachment.class);
        when(message.getAttachment()).thenReturn(attachment);
        when(attachment.getSelectedOption()).thenReturn(null);
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, true);

        assertTrue(result);
    }

    @Test
    public void execute_returnsTrue_whenSelectedOptionIsEmpty() {
        ChatMessage message = mock(ChatMessage.class);
        SingleChoiceAttachment attachment = mock(SingleChoiceAttachment.class);
        when(message.getAttachment()).thenReturn(attachment);
        when(attachment.getSelectedOption()).thenReturn("");
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, true);

        assertTrue(result);
    }

    @Test
    public void execute_returnsFalse_whenSelectedOptionIsPresent() {
        ChatMessage message = mock(ChatMessage.class);
        SingleChoiceAttachment attachment = mock(SingleChoiceAttachment.class);
        when(message.getAttachment()).thenReturn(attachment);
        when(attachment.getSelectedOption()).thenReturn("selectedValue");
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, true);

        assertFalse(result);
    }

    @Test
    public void execute_returnsFalse_whenAdapterIsNull() {
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        CustomCardShouldShowUseCase useCase = new CustomCardShouldShowUseCase(null);

        boolean result = useCase.execute(message, VIEW_TYPE, false);

        assertFalse(result);
    }

    @Test
    public void execute_returnsFalse_whenMetadataIsNull() {
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(null);
        when(customCardAdapter.shouldShowCard(any(ChatMessage.class), anyInt())).thenReturn(true);

        boolean result = useCase.execute(message, VIEW_TYPE, false);

        assertFalse(result);
    }

    @Test
    public void execute_returnsFalse_whenCustomCardAdapterReturnsFalse() {
        ChatMessage message = mock(ChatMessage.class);
        when(message.getId()).thenReturn("id");
        when(message.getContent()).thenReturn("");
        when(message.getSenderType()).thenReturn(Chat.Participant.OPERATOR);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(customCardAdapter.shouldShowCard(any(ChatMessage.class), anyInt())).thenReturn(false);

        boolean result = useCase.execute(message, VIEW_TYPE, false);

        assertFalse(result);
    }

    @Test
    public void execute_returnsTrue_whenCustomCardAdapterReturnsTrue() {
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id");
        when(message.getContent()).thenReturn("");
        when(message.getSenderType()).thenReturn(Chat.Participant.OPERATOR);
        when(customCardAdapter.shouldShowCard(any(ChatMessage.class), anyInt())).thenReturn(true);

        boolean result = useCase.execute(message, VIEW_TYPE, false);

        assertTrue(result);
    }

    @Test
    public void execute_callAdapter_whenAdapterNotNull() {
        ChatMessage message = mock(ChatMessage.class);
        when(message.getId()).thenReturn("id");
        when(message.getContent()).thenReturn("");
        when(message.getSenderType()).thenReturn(Chat.Participant.OPERATOR);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(customCardAdapter.shouldShowCard(any(ChatMessage.class), anyInt())).thenReturn(true);

        useCase.execute(message, VIEW_TYPE, false);

        verify(customCardAdapter).shouldShowCard(any(ChatMessage.class), eq(VIEW_TYPE));
    }

}
