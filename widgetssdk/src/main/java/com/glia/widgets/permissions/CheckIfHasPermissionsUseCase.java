package com.glia.widgets.permissions;

import com.glia.widgets.model.PermissionType;
import com.glia.widgets.model.PermissionsManager;

public class CheckIfHasPermissionsUseCase {

    private final PermissionsManager permissionsManager;

    public CheckIfHasPermissionsUseCase(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public boolean execute(PermissionType permissionType){
        return permissionsManager.checkIfHasPermission(permissionType);
    }
}
