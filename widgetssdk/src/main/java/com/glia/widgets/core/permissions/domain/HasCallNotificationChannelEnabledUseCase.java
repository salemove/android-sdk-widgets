package com.glia.widgets.core.permissions.domain;

import com.glia.widgets.core.permissions.PermissionManager;

public class HasCallNotificationChannelEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasCallNotificationChannelEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return this.permissionManager.hasCallNotificationChannelEnabled();
    }
}
