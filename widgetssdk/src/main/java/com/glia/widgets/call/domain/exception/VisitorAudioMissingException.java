package com.glia.widgets.call.domain.exception;

public class VisitorAudioMissingException extends RuntimeException {
    public VisitorAudioMissingException() {
        super("Visitor Audio Media state missing");
    }
}
