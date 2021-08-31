package com.glia.widgets.filepreview.domain.exception;

public class CacheFileNotFoundException extends FileNotFoundException {
    public CacheFileNotFoundException() {
        super("File not found in cache");
    }
}
