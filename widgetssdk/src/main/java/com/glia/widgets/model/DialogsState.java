package com.glia.widgets.model;

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

    class EndEngagementDialog implements DialogsState {
        public final String operatorName;

        public EndEngagementDialog(String operatorName) {
            this.operatorName = operatorName;
        }
    }

    class UpgradeAudioDialog implements DialogsState {
        public final String operatorName;

        public UpgradeAudioDialog(String operatorName) {
            this.operatorName = operatorName;
        }
    }
}
