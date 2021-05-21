package com.glia.widgets.permissions;

import com.glia.widgets.model.PermissionType;
import com.glia.widgets.model.PermissionsManager;

public class CheckIfShowPermissionsDialogUseCase {
    private final PermissionsManager permissionsManager;

    public CheckIfShowPermissionsDialogUseCase(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public boolean execute(PermissionType permissionType, boolean checkIfShownOnce) {
        return permissionsManager.shouldShowPermissionsDialog(permissionType, checkIfShownOnce);
    }
}
