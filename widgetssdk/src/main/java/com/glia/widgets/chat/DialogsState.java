package com.glia.widgets.chat;

public class DialogsState {

    public final boolean overlayPermissionsShowing;
    public final boolean noOperatorsAvailableDialogShowing;
    public final boolean unexpectedErrorDialogShowing;
    public final boolean exitDialogShowing;

    public DialogsState(boolean overlayPermissionsShowing,
                        boolean noOperatorsAvailableDialogShowing,
                        boolean unexpectedErrorDialogShowing,
                        boolean exitDialogShowing) {
        this.overlayPermissionsShowing = overlayPermissionsShowing;
        this.noOperatorsAvailableDialogShowing = noOperatorsAvailableDialogShowing;
        this.unexpectedErrorDialogShowing = unexpectedErrorDialogShowing;
        this.exitDialogShowing = exitDialogShowing;
    }

    public boolean isDialogShowing() {
        return overlayPermissionsShowing || noOperatorsAvailableDialogShowing ||
                unexpectedErrorDialogShowing || exitDialogShowing;
    }
}
