package com.glia.widgets.core.dialog.domain;

import com.glia.widgets.core.dialog.PermissionDialogManager;

public class SetEnableCallNotificationChannelDialogShownUseCase {
    public final PermissionDialogManager permissionDialogManager;

    public SetEnableCallNotificationChannelDialogShownUseCase(PermissionDialogManager permissionDialogManager) {
        this.permissionDialogManager = permissionDialogManager;
    }

    public void execute() {
        this.permissionDialogManager.setEnableCallNotificationChannelRequestShown();
    }
}
