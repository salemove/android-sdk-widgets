package com.glia.widgets.dialog.domain;

import com.glia.widgets.dialog.PermissionDialogManager;
import com.glia.widgets.permissions.PermissionManager;

public class IsShowEnableCallNotificationChannelDialogUseCase {
    private final PermissionDialogManager permissionDialogManager;
    private final PermissionManager permissionManager;

    public IsShowEnableCallNotificationChannelDialogUseCase(
            PermissionManager permissionManager,
            PermissionDialogManager permissionDialogManager
    ) {
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
    }

    public boolean execute() {
        return isCallNotificationChannelNotEnabled() && hasNotShownCallNotificationNotEnabledRequest();
    }

    private boolean isCallNotificationChannelNotEnabled() {
        return !this.permissionManager.hasCallNotificationChannelEnabled();
    }

    private boolean hasNotShownCallNotificationNotEnabledRequest() {
        return !this.permissionDialogManager.hasEnableCallNotificationChannelRequestShown();
    }
}
