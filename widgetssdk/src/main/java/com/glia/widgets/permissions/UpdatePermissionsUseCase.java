package com.glia.widgets.permissions;

import com.glia.widgets.model.PermissionsManager;

public class UpdatePermissionsUseCase {
    private final PermissionsManager permissionsManager;

    public UpdatePermissionsUseCase(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public void execute(
            boolean hasOverlayPermissions,
            boolean isCallNotificationChannelEnabled,
            boolean isScreenSharingNotificationChannelEnabled
    ) {
        permissionsManager.setOverlayPermissions(hasOverlayPermissions);
        permissionsManager.updateNotificationChannelStatuses(
                isCallNotificationChannelEnabled,
                isScreenSharingNotificationChannelEnabled
        );
    }
}
