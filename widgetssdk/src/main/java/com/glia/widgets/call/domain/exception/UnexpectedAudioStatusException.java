package com.glia.widgets.call.domain.exception;

import com.glia.androidsdk.comms.Media;

public class UnexpectedAudioStatusException extends IllegalStateException {
    public UnexpectedAudioStatusException(Media.Status status) {
        super("Unexpected value: " + status);
    }
}
