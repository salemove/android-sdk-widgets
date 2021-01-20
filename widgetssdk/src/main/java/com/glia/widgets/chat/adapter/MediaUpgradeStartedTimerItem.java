package com.glia.widgets.chat.adapter;

import java.util.Objects;

public class MediaUpgradeStartedTimerItem extends ChatItem {
    public final static String ID = "media_upgrade_item";
    public final MediaUpgradeStartedTimerItem.Type type;
    public final String time;

    public MediaUpgradeStartedTimerItem(MediaUpgradeStartedTimerItem.Type type, String time) {
        super(ID, ChatAdapter.MEDIA_UPGRADE_ITEM_TYPE);
        this.type = type;
        this.time = time;
    }

    public enum Type {
        AUDIO, VIDEO
    }

    @Override
    public String toString() {
        return "MediaUpgradeStartedTimerItem{" +
                "type=" + type +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MediaUpgradeStartedTimerItem that = (MediaUpgradeStartedTimerItem) o;
        return type == that.type &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, time);
    }
}
