package com.glia.widgets.model;

import com.glia.widgets.view.DialogOfferType;

public interface DialogsState {
    boolean showingChatEnderDialog();

    class NoDialog implements DialogsState {
        @Override
        public boolean showingChatEnderDialog() {
            return false;
        }
    }

    class OverlayPermissionsDialog implements DialogsState {
        @Override
        public boolean showingChatEnderDialog() {
            return false;
        }
    }

    class UnexpectedErrorDialog implements DialogsState {
        @Override
        public boolean showingChatEnderDialog() {
            return true;
        }
    }

    class NoMoreOperatorsDialog implements DialogsState {
        @Override
        public boolean showingChatEnderDialog() {
            return true;
        }
    }

    class ExitQueueDialog implements DialogsState {
        @Override
        public boolean showingChatEnderDialog() {
            return false;
        }
    }

    class EndEngagementDialog implements DialogsState {
        public final String operatorName;

        public EndEngagementDialog(String operatorName) {
            this.operatorName = operatorName;
        }

        @Override
        public boolean showingChatEnderDialog() {
            return false;
        }
    }

    class UpgradeDialog implements DialogsState {
        public final DialogOfferType type;

        public UpgradeDialog(DialogOfferType type) {
            this.type = type;
        }

        @Override
        public boolean showingChatEnderDialog() {
            return false;
        }
    }
}
