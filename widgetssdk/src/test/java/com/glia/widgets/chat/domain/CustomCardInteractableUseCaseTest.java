package com.glia.widgets.chat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.model.ChatInputMode;
import com.glia.widgets.chat.model.history.ChatItem;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CustomCardInteractableUseCaseTest {
    private static final Integer ADAPTER_VIEW_TYPE = 10;
    private static final Integer CUSTOM_VIEW_TYPE = 100;

    private CustomCardAdapter customCardAdapter;
    private CustomCardInteractableUseCase useCase;

    @Before
    public void setUp() {
        customCardAdapter = mock(CustomCardAdapter.class);
        when(customCardAdapter.getChatAdapterViewType(any())).thenReturn(null);
        useCase = new CustomCardInteractableUseCase(customCardAdapter);
    }

    @Test
    public void execute_callsGetCustomCardViewType_ifItemInList() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(ADAPTER_VIEW_TYPE);
        when(customCardAdapter.isInteractable(any(ChatMessage.class), anyInt())).thenReturn(true);

        useCase.execute(chatItems, message);

        verify(customCardAdapter).getCustomCardViewType(1);
    }

    @Test
    public void execute_notCallsGetCustomCardViewType_ifItemNotInList() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("anotherId");

        useCase.execute(chatItems, message);

        verify(customCardAdapter, never()).getCustomCardViewType(anyInt());
    }

    @Test
    public void execute_callsIsInteractable_ifAdapterViewTypeNotNull() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(ADAPTER_VIEW_TYPE);

        useCase.execute(chatItems, message);

        verify(customCardAdapter).isInteractable(message, ADAPTER_VIEW_TYPE);
    }

    @Test
    public void execute_notCallsIsInteractable_ifAdapterViewTypeIsNull() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(null);
        when(customCardAdapter.isInteractable(any(ChatMessage.class), anyInt())).thenReturn(true);

        useCase.execute(chatItems, message);

        verify(customCardAdapter, never()).isInteractable(any(ChatMessage.class), anyInt());
    }

    @Test
    public void execute_returnSingleChoiceCard_ifAdapterReturnsTrue() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(ADAPTER_VIEW_TYPE);
        when(customCardAdapter.isInteractable(any(ChatMessage.class), anyInt())).thenReturn(true);

        ChatInputMode result = useCase.execute(chatItems, message);

        assertEquals(ChatInputMode.SINGLE_CHOICE_CARD, result);
    }

    @Test
    public void execute_returnEnable_ifAdapterReturnsFalse() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(ADAPTER_VIEW_TYPE);
        when(customCardAdapter.isInteractable(any(ChatMessage.class), anyInt())).thenReturn(false);

        ChatInputMode result = useCase.execute(chatItems, message);

        assertEquals(ChatInputMode.ENABLED, result);
    }

    @Test
    public void execute_returnsNull_ifItemNotInList() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("anotherId");

        ChatInputMode result = useCase.execute(chatItems, message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenAdapterViewTypeIsNull() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());
        when(message.getId()).thenReturn("id1");
        when(customCardAdapter.getCustomCardViewType(anyInt())).thenReturn(null);
        when(customCardAdapter.isInteractable(any(ChatMessage.class), anyInt())).thenReturn(true);

        ChatInputMode result = useCase.execute(chatItems, message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenAdapterIsNull() {
        CustomCardInteractableUseCase useCase = new CustomCardInteractableUseCase(null);
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(new JSONObject());

        ChatInputMode result = useCase.execute(chatItems, message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenMetadataIsNull() {
        List<ChatItem> chatItems = makeChatItems();
        ChatMessage message = mock(ChatMessage.class);
        when(message.getMetadata()).thenReturn(null);

        ChatInputMode result = useCase.execute(chatItems, message);

        assertNull(result);
    }

    private List<ChatItem> makeChatItems() {
        List<ChatItem> items = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ChatItem item = mock(ChatItem.class);
            when(item.getId()).thenReturn("id" + i);
            when(item.getViewType()).thenReturn(i);
            items.add(item);
        }
        return items;
    }
}
