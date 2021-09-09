package com.glia.widgets.core.dialog;

import com.glia.widgets.view.DialogOfferType;

public interface DialogsState {

    class NoDialog implements DialogsState {
    }

    class OverlayPermissionsDialog implements DialogsState {
    }

    class UnexpectedErrorDialog implements DialogsState {
    }

    class NoMoreOperatorsDialog implements DialogsState {
    }

    class ExitQueueDialog implements DialogsState {
    }

    class StartScreenSharingDialog implements DialogsState {
    }

    class EndScreenSharingDialog implements DialogsState {
    }

    class EndEngagementDialog implements DialogsState {
        public final String operatorName;

        public EndEngagementDialog(String operatorName) {
            this.operatorName = operatorName;
        }
    }

    class UpgradeDialog implements DialogsState {
        public final DialogOfferType type;

        public UpgradeDialog(DialogOfferType type) {
            this.type = type;
        }
    }

    class EnableNotificationChannelDialog implements DialogsState {

    }

    class EnableScreenSharingNotificationsAndStartSharingDialog implements DialogsState {

    }
}
