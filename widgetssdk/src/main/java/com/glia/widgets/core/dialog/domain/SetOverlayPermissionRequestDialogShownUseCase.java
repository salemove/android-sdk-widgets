package com.glia.widgets.core.dialog.domain;

import com.glia.widgets.core.dialog.PermissionDialogManager;

public class SetOverlayPermissionRequestDialogShownUseCase {
    private final PermissionDialogManager permissionDialogManager;

    public SetOverlayPermissionRequestDialogShownUseCase(PermissionDialogManager permissionDialogManager) {
        this.permissionDialogManager = permissionDialogManager;
    }

    public void execute() {
        this.permissionDialogManager.setOverlayPermissionDialogShown();
    }
}
