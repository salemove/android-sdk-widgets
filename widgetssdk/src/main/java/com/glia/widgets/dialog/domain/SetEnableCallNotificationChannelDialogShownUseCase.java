package com.glia.widgets.dialog.domain;

import com.glia.widgets.dialog.PermissionDialogManager;

public class SetEnableCallNotificationChannelDialogShownUseCase {
    public final PermissionDialogManager permissionDialogManager;
    public SetEnableCallNotificationChannelDialogShownUseCase(PermissionDialogManager permissionDialogManager) {
        this.permissionDialogManager = permissionDialogManager;
    }

    public void execute() {
        this.permissionDialogManager.setEnableCallNotificationChannelRequestShown();
    }
}
