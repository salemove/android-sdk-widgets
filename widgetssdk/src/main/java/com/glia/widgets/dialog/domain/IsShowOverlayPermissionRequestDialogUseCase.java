package com.glia.widgets.dialog.domain;

import com.glia.widgets.dialog.PermissionDialogManager;
import com.glia.widgets.permissions.PermissionManager;

public class IsShowOverlayPermissionRequestDialogUseCase {
    private final PermissionDialogManager permissionDialogManager;
    private final PermissionManager permissionManager;

    public IsShowOverlayPermissionRequestDialogUseCase(
            PermissionManager permissionManager,
            PermissionDialogManager permissionDialogManager
    ) {
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
    }

    public boolean execute() {
        return hasNoOverlayPermissions() && hasNotShownOverlayPermissionRequest();
    }

    private boolean hasNoOverlayPermissions() {
        return !permissionManager.hasOverlayPermission();
    }

    private boolean hasNotShownOverlayPermissionRequest() {
        return !permissionDialogManager.hasOverlayPermissionDialogShown();
    }
}
