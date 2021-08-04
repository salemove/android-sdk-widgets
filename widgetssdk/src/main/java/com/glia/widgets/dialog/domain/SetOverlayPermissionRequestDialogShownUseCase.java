package com.glia.widgets.dialog.domain;

import com.glia.widgets.dialog.PermissionDialogManager;

public class SetOverlayPermissionRequestDialogShownUseCase {
    private final PermissionDialogManager permissionDialogManager;

    public SetOverlayPermissionRequestDialogShownUseCase(PermissionDialogManager permissionDialogManager) {
        this.permissionDialogManager = permissionDialogManager;
    }

    public void execute() {
        this.permissionDialogManager.setOverlayPermissionDialogShown();
    }
}
