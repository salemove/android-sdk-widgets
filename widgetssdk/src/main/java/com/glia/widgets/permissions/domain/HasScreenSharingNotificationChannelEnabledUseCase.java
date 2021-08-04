package com.glia.widgets.permissions.domain;

import com.glia.widgets.permissions.PermissionManager;

public class HasScreenSharingNotificationChannelEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasScreenSharingNotificationChannelEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return permissionManager.hasScreenSharingNotificationChannelEnabled();
    }
}
