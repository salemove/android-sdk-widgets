package com.glia.widgets.call.domain.exception;

import com.glia.androidsdk.comms.Media;

public class UnexpectedVideoStatusException extends IllegalStateException {
    public UnexpectedVideoStatusException(Media.Status status) {
        super("Unexpected value: " + status);
    }
}
