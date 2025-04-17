package com.glia.widgets.chat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.chat.Chat;
import com.glia.widgets.chat.adapter.CustomCardAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class CustomCardAdapterTypeUseCaseTest {
    private static final Integer VIEW_TYPE = 10;

    private CustomCardAdapter customCardAdapter;
    private CustomCardAdapterTypeUseCase useCase;

    @Before
    public void setUp() {
        customCardAdapter = mock(CustomCardAdapter.class);
        when(customCardAdapter.getChatAdapterViewType(any())).thenReturn(null);
        useCase = new CustomCardAdapterTypeUseCase(customCardAdapter);
    }

    @Test
    public void execute_returnsNull_whenAdapterIsNull() {
        CustomCardAdapterTypeUseCase useCase = new CustomCardAdapterTypeUseCase(null);
        com.glia.androidsdk.chat.ChatMessage message = mock(com.glia.androidsdk.chat.ChatMessage.class);

        Integer result = useCase.invoke(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenMetadataIsNull() {
        com.glia.androidsdk.chat.ChatMessage message = mock(com.glia.androidsdk.chat.ChatMessage.class);

        Integer result = useCase.invoke(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenAdapterReturnsNull() {
        com.glia.androidsdk.chat.ChatMessage message = mock(com.glia.androidsdk.chat.ChatMessage.class);
        JSONObject metadata = new JSONObject();
        when(message.getMetadata()).thenReturn(metadata);

        Integer result = useCase.invoke(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsViewType_whenAdapterReturnsViewType() throws JSONException {
        com.glia.androidsdk.chat.ChatMessage message = mock(com.glia.androidsdk.chat.ChatMessage.class);
        JSONObject metadata = new JSONObject().put("someKey", "someValue");
        when(message.getMetadata()).thenReturn(metadata);
        when(message.getId()).thenReturn("id");
        when(message.getContent()).thenReturn("");
        when(message.getSenderType()).thenReturn(Chat.Participant.OPERATOR);
//        when(message.getTimestamp()).thenReturn(-1L);
        when(customCardAdapter.getChatAdapterViewType(any())).thenReturn(VIEW_TYPE);

        Integer result = useCase.invoke(message);

        assertEquals(VIEW_TYPE, result);
    }
}
