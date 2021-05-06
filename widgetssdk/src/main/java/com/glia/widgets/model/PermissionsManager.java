package com.glia.widgets.model;

public class PermissionsManager {
    private PermissionsState permissionsState;

    public PermissionsManager() {
        permissionsState = PermissionsState.reset();
    }

    public boolean shouldShowPermissionsDialog(PermissionType permissionType, boolean checkIfShownOnce) {
        if (permissionType == PermissionType.OVERLAY) {
            return shouldShowOverlayPermissionsDialog(checkIfShownOnce);
        } else if (permissionType == PermissionType.CALL_CHANNEL) {
            return shouldShowCallNotificationChannelDialog(checkIfShownOnce);
        } else if (permissionType == PermissionType.SCREEN_SHARING_CHANNEL) {
            return shouldShowScreenSharingNotificationChannelDialog(checkIfShownOnce);
        } else {
            throw new IllegalArgumentException("Unknown permission type: " + permissionType);
        }
    }

    public void setOverlayPermissions(boolean hasOverlayPermissions) {
        this.permissionsState = permissionsState.hasOverlayPermissionsChanged(hasOverlayPermissions);
    }

    public void updateNotificationChannelStatuses(
            boolean isCallChannelEnabled,
            boolean isScreenSharingChannelEnabled
    ) {
        this.permissionsState = permissionsState.channelStatusesChanged(isScreenSharingChannelEnabled, isCallChannelEnabled);
    }

    public void updateDialogShown(PermissionType permissionType) {
        if (permissionType == PermissionType.CALL_CHANNEL ||
                permissionType == PermissionType.SCREEN_SHARING_CHANNEL) {
            notificationsChannelDialogShown();
        } else if (permissionType == PermissionType.OVERLAY) {
            overlayPermissionsDialogShown();
        }
    }

    public void reset() {
        permissionsState = PermissionsState.reset();
    }

    private boolean shouldShowOverlayPermissionsDialog(boolean checkIfShownOnce) {
        return !permissionsState.hasOverlayPermissions &&
                (!checkIfShownOnce || !permissionsState.isOverlayPermissionsDialogShown);
    }

    private boolean shouldShowCallNotificationChannelDialog(boolean checkIfShownOnce) {
        return !permissionsState.isCallChannelEnabled &&
                (!checkIfShownOnce || !permissionsState.isNotificationPermissionsDialogShown);
    }

    private boolean shouldShowScreenSharingNotificationChannelDialog(boolean checkIfShownOnce) {
        return !permissionsState.isScreenSharingChannelEnabled &&
                (!checkIfShownOnce || !permissionsState.isNotificationPermissionsDialogShown);
    }

    private void overlayPermissionsDialogShown() {
        permissionsState = permissionsState.overlayPermissionDialogShown();
    }


    private void notificationsChannelDialogShown() {
        permissionsState = permissionsState.notificationPermissionsDialogShown();
    }
}
