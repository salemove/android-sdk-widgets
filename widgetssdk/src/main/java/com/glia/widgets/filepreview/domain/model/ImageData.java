package com.glia.widgets.filepreview.domain.model;

import android.graphics.Bitmap;

public class ImageData {
    private final String name;
    private final Bitmap image;
    private final FromLocation fromLocation;

    public ImageData(String name, Bitmap image, FromLocation fromLocation) {
        this.name = name;
        this.image = image;
        this.fromLocation = fromLocation;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }

    public FromLocation fromLocation() {
        return fromLocation;
    }

    public enum FromLocation {
        DOWNLOADS,
        CACHE,
        NETWORK
    }
}
