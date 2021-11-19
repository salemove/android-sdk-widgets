package com.glia.widgets.core.dialog.domain;

import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.permissions.PermissionManager;

public class IsShowOverlayPermissionRequestDialogUseCase {
    private final PermissionDialogManager permissionDialogManager;
    private final PermissionManager permissionManager;
    private final GliaSdkConfigurationManager gliaSdkConfigurationManager;

    public IsShowOverlayPermissionRequestDialogUseCase(
            PermissionManager permissionManager,
            PermissionDialogManager permissionDialogManager,
            GliaSdkConfigurationManager gliaSdkConfigurationManager
    ) {
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
        this.gliaSdkConfigurationManager = gliaSdkConfigurationManager;
    }

    public boolean execute() {
        return hasNoOverlayPermissions() &&
                hasNotShownOverlayPermissionRequest() &&
                isUseOverlay();
    }

    private boolean hasNoOverlayPermissions() {
        return !permissionManager.hasOverlayPermission();
    }

    private boolean hasNotShownOverlayPermissionRequest() {
        return !permissionDialogManager.hasOverlayPermissionDialogShown();
    }

    private boolean isUseOverlay() {
        return gliaSdkConfigurationManager.isUseOverlay();
    }
}
