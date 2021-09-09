package com.glia.widgets.core.permissions.domain;

import com.glia.widgets.core.permissions.PermissionManager;

public class HasScreenSharingNotificationChannelEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasScreenSharingNotificationChannelEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return permissionManager.hasScreenSharingNotificationChannelEnabled();
    }
}
