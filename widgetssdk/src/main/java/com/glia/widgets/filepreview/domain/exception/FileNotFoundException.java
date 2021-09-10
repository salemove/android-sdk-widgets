package com.glia.widgets.filepreview.domain.exception;

public class FileNotFoundException extends RuntimeException {
    FileNotFoundException() {
        super();
    }

    FileNotFoundException(String message) {
        super(message);
    }
}
