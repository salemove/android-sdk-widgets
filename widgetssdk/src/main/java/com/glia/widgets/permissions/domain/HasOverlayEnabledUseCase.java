package com.glia.widgets.permissions.domain;

import com.glia.widgets.permissions.PermissionManager;

public class HasOverlayEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasOverlayEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return permissionManager.hasOverlayPermission();
    }
}
