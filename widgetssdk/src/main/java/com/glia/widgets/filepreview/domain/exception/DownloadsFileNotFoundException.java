package com.glia.widgets.filepreview.domain.exception;

public class DownloadsFileNotFoundException extends FileNotFoundException {
    public DownloadsFileNotFoundException() {
        super("File not found in downloads folder");
    }
}
