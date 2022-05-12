package com.glia.widgets.call.domain.exception;

public class VisitorVideoMissingException extends RuntimeException {
    public VisitorVideoMissingException() {
        super("Visitor Video Media state missing");
    }
}
