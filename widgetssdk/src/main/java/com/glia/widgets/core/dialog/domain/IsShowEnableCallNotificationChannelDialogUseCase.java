package com.glia.widgets.core.dialog.domain;

import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.permissions.PermissionManager;

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
