package com.glia.widgets.chat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.adapter.CustomCardAdapter;

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
        ChatMessage message = mock(ChatMessage.class);

        Integer result = useCase.execute(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenMetadataIsNull() {
        ChatMessage message = mock(ChatMessage.class);

        Integer result = useCase.execute(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsNull_whenAdapterReturnsNull() {
        ChatMessage message = mock(ChatMessage.class);
        JSONObject metadata = new JSONObject();
        when(message.getMetadata()).thenReturn(metadata);

        Integer result = useCase.execute(message);

        assertNull(result);
    }

    @Test
    public void execute_returnsViewType_whenAdapterReturnsViewType() {
        ChatMessage message = mock(ChatMessage.class);
        JSONObject metadata = new JSONObject();
        when(message.getMetadata()).thenReturn(metadata);
        when(customCardAdapter.getChatAdapterViewType(message)).thenReturn(VIEW_TYPE);

        Integer result = useCase.execute(message);

        assertEquals(VIEW_TYPE, result);
    }
}
