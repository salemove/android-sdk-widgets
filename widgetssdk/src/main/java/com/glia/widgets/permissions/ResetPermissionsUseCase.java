package com.glia.widgets.permissions;

import com.glia.widgets.model.PermissionsManager;

public class ResetPermissionsUseCase {
    private final PermissionsManager permissionsManager;

    public ResetPermissionsUseCase(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public void execute() {
        permissionsManager.reset();
    }
}
