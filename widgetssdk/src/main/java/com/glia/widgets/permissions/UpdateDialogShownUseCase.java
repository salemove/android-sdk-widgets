package com.glia.widgets.permissions;

import com.glia.widgets.model.PermissionType;
import com.glia.widgets.model.PermissionsManager;

public class UpdateDialogShownUseCase {
    private final PermissionsManager permissionsManager;

    public UpdateDialogShownUseCase(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public void execute(PermissionType permissionType) {
        permissionsManager.updateDialogShown(permissionType);
    }
}
