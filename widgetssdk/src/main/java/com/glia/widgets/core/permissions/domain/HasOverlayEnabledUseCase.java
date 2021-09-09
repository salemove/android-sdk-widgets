package com.glia.widgets.core.permissions.domain;

import com.glia.widgets.core.permissions.PermissionManager;

public class HasOverlayEnabledUseCase {
    private final PermissionManager permissionManager;

    public HasOverlayEnabledUseCase(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean execute() {
        return permissionManager.hasOverlayPermission();
    }
}
