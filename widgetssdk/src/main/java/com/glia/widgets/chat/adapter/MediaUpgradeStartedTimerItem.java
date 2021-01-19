package com.glia.widgets.chat.adapter;

public class MediaUpgradeStartedTimerItem extends ChatItem {

    public final MediaUpgradeStartedTimerItem.Type type;
    public final String time;

    public MediaUpgradeStartedTimerItem(MediaUpgradeStartedTimerItem.Type type, String time) {
        super(ChatAdapter.MEDIA_UPGRADE_ITEM_TYPE);
        this.type = type;
        this.time = time;
    }

    public enum Type {
        AUDIO, VIDEO
    }
}
