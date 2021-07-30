package com.glia.widgets.permissions.domain;

import com.glia.widgets.permissions.PermissionManager;

public class HasCallNotificationChannelEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasCallNotificationChannelEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return this.permissionManager.hasCallNotificationChannelEnabled();
    }
}
