package com.glia.widgets.chat.adapter;

import static com.glia.widgets.chat.adapter.ChatAdapter.CUSTOM_CARD_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder;

import org.junit.Before;
import org.junit.Test;

public class CustomCardAdapterTest {
    private static final Integer CUSTOM_VIEW_TYPE_1 = 10;
    private static final Integer CUSTOM_VIEW_TYPE_2 = 11;
    private static final Integer CUSTOM_VIEW_TYPE_3 = 12;
    private static final Integer CUSTOM_VIEW_TYPE_4 = 13;
    private static final Integer ADAPTER_VIEW_TYPE_1 = 100;
    private static final Integer ADAPTER_VIEW_TYPE_2 = 101;
    private static final Integer ADAPTER_VIEW_TYPE_3 = 102;

    private TestableCustomCardAdapter adapter;

    @Before
    public void setUp() {
        adapter = new TestableCustomCardAdapter();
    }

    @Test
    public void getChatAdapterViewType_returnsNull_whenViewTypeIsNull() {
        ChatMessage message = new ChatMessage("1", "text", 1L, mock(), null, null, null);

        adapter.customViewType = null;
        Integer result = adapter.getChatAdapterViewType(message);

        assertNull(result);
    }

    @Test
    public void getChatAdapterViewType_returnsValueFromMap_ifExists() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_2, ADAPTER_VIEW_TYPE_2);
        ChatMessage message = new ChatMessage("1", "text", 1L, mock(), null, null, null);

        adapter.customViewType = CUSTOM_VIEW_TYPE_1;
        Integer result = adapter.getChatAdapterViewType(message);

        assertEquals(ADAPTER_VIEW_TYPE_1, result);

        adapter.customViewType = CUSTOM_VIEW_TYPE_2;
        result = adapter.getChatAdapterViewType(message);

        assertEquals(ADAPTER_VIEW_TYPE_2, result);
    }

    @Test
    public void getChatAdapterViewType_returnsNewValue_ifItIsNewCustomType() {
        ChatMessage message = new ChatMessage("1", "text", 1L, mock(), null, null, null);

        adapter.customViewType = CUSTOM_VIEW_TYPE_3;
        Integer result = adapter.getChatAdapterViewType(message);

        assertEquals(CUSTOM_CARD_TYPE, result.intValue());
    }

    @Test
    public void getChatAdapterViewType_returnsNewValue_forNewCustomType() {
        ChatMessage message = new ChatMessage("1", "text", 1L, mock(), null, null, null);

        adapter.customViewType = CUSTOM_VIEW_TYPE_3;
        Integer ignore = adapter.getChatAdapterViewType(message);

        adapter.customViewType = CUSTOM_VIEW_TYPE_4;
        Integer result = adapter.getChatAdapterViewType(message);

        assertEquals(CUSTOM_CARD_TYPE + 1, result.intValue());
    }

    @Test
    public void getCustomCardViewType_returnsNull_ifItNotContainsInMap() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_2, ADAPTER_VIEW_TYPE_2);

        Integer result = adapter.getCustomCardViewType(ADAPTER_VIEW_TYPE_3);
        assertNull(result);
    }

    @Test
    public void getCustomCardViewType_returnsValue_ifItContainsMap() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_2, ADAPTER_VIEW_TYPE_2);

        Integer result = adapter.getCustomCardViewType(ADAPTER_VIEW_TYPE_1);
        assertEquals(CUSTOM_VIEW_TYPE_1, result);

        result = adapter.getCustomCardViewType(ADAPTER_VIEW_TYPE_2);
        assertEquals(CUSTOM_VIEW_TYPE_2, result);
    }

    @Test
    public void getCustomCardViewHolder_callsOnCreateViewHolder_ifCustomViewTypeNotNull() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);

        ViewGroup parent = mock(ViewGroup.class);
        LayoutInflater inflater = mock(LayoutInflater.class);

        adapter.getCustomCardViewHolder(parent, inflater, ADAPTER_VIEW_TYPE_1);

        assertTrue(adapter.isOnCreateViewHolderIsCalled);
    }

    @Test
    public void getCustomCardViewHolder_notCallsOnCreateViewHolder_ifCustomViewTypeIsNull() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);

        ViewGroup parent = mock(ViewGroup.class);
        LayoutInflater inflater = mock(LayoutInflater.class);

        adapter.getCustomCardViewHolder(parent, inflater, ADAPTER_VIEW_TYPE_2);

        assertFalse(adapter.isOnCreateViewHolderIsCalled);
    }

    @Test
    public void getCustomCardViewHolder_returnsViewHolder_ifCustomViewTypeNotNull() {
        adapter.viewTypeMap.put(CUSTOM_VIEW_TYPE_1, ADAPTER_VIEW_TYPE_1);

        ViewGroup parent = mock(ViewGroup.class);
        LayoutInflater inflater = mock(LayoutInflater.class);
        CustomCardViewHolder viewHolder = mock(CustomCardViewHolder.class);

        adapter.viewHolderCallback = (receivedParent, receivedInflater, receivedViewType) -> {
            assertEquals(parent, receivedParent);
            assertEquals(inflater, receivedInflater);
            assertEquals(CUSTOM_VIEW_TYPE_1.intValue(), receivedViewType);
            return viewHolder;
        };

        CustomCardViewHolder value = adapter.getCustomCardViewHolder(parent, inflater, ADAPTER_VIEW_TYPE_1);

        assertEquals(viewHolder, value);
    }

    static class TestableCustomCardAdapter extends CustomCardAdapter {
        public Integer customViewType;
        public ViewHolderCallback viewHolderCallback;
        public boolean isOnCreateViewHolderIsCalled = false;

        @Nullable
        @Override
        public Integer getItemViewType(CustomCardMessage message) {
            return customViewType;
        }

        @NonNull
        @Override
        public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       @NonNull LayoutInflater inflater,
                                                       int viewType) {
            isOnCreateViewHolderIsCalled = true;
            if (viewHolderCallback == null) return null;
            return viewHolderCallback.onCreateViewHolder(parent, inflater, viewType);
        }

        interface ViewHolderCallback {
            CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                    @NonNull LayoutInflater inflater,
                                                    int viewType);
        }
    }
}
