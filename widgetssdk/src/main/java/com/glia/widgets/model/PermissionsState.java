package com.glia.widgets.model;

import java.util.Objects;

class PermissionsState {
    public final boolean isScreenSharingChannelEnabled;
    public final boolean isCallChannelEnabled;
    public final boolean hasOverlayPermissions;
    public final boolean isOverlayPermissionsDialogShown;
    public final boolean isNotificationPermissionsDialogShown;

    private PermissionsState(boolean isScreenSharingChannelEnabled,
                             boolean isCallChannelEnabled,
                             boolean hasOverlayPermissions,
                             boolean isOverlayPermissionsDialogShown,
                             boolean isNotificationPermissionsDialogShown
    ) {
        this.isScreenSharingChannelEnabled = isScreenSharingChannelEnabled;
        this.isCallChannelEnabled = isCallChannelEnabled;
        this.hasOverlayPermissions = hasOverlayPermissions;
        this.isOverlayPermissionsDialogShown = isOverlayPermissionsDialogShown;
        this.isNotificationPermissionsDialogShown = isNotificationPermissionsDialogShown;
    }

    public static PermissionsState reset() {
        return new PermissionsState(
                true,
                true,
                false,
                false,
                false
        );
    }

    public PermissionsState channelStatusesChanged(boolean isScreenSharingChannelEnabled, boolean isCallChannelEnabled) {
        return new PermissionsState(
                isScreenSharingChannelEnabled,
                isCallChannelEnabled,
                this.hasOverlayPermissions,
                this.isOverlayPermissionsDialogShown,
                this.isNotificationPermissionsDialogShown
        );
    }

    public PermissionsState hasOverlayPermissionsChanged(boolean hasOverlayPermissions) {
        return new PermissionsState(
                this.isScreenSharingChannelEnabled,
                this.isCallChannelEnabled,
                hasOverlayPermissions,
                this.isOverlayPermissionsDialogShown,
                this.isNotificationPermissionsDialogShown
        );
    }

    public PermissionsState overlayPermissionDialogShown() {
        return new PermissionsState(
                this.isScreenSharingChannelEnabled,
                this.isCallChannelEnabled,
                this.hasOverlayPermissions,
                true,
                this.isNotificationPermissionsDialogShown
        );
    }

    public PermissionsState notificationPermissionsDialogShown() {
        return new PermissionsState(
                this.isScreenSharingChannelEnabled,
                this.isCallChannelEnabled,
                this.hasOverlayPermissions,
                this.isOverlayPermissionsDialogShown,
                true
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionsState that = (PermissionsState) o;
        return isScreenSharingChannelEnabled == that.isScreenSharingChannelEnabled &&
                isCallChannelEnabled == that.isCallChannelEnabled &&
                hasOverlayPermissions == that.hasOverlayPermissions &&
                isOverlayPermissionsDialogShown == that.isOverlayPermissionsDialogShown &&
                isNotificationPermissionsDialogShown == that.isNotificationPermissionsDialogShown;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isScreenSharingChannelEnabled, isCallChannelEnabled, hasOverlayPermissions, isOverlayPermissionsDialogShown, isNotificationPermissionsDialogShown);
    }

    @Override
    public String toString() {
        return "PermissionsState{" +
                "isScreenSharingChannelEnabled=" + isScreenSharingChannelEnabled +
                ", isCallChannelEnabled=" + isCallChannelEnabled +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
                ", isOverlayPermissionsDialogShown=" + isOverlayPermissionsDialogShown +
                ", isNotificationPermissionsDialogShown=" + isNotificationPermissionsDialogShown +
                '}';
    }
}
