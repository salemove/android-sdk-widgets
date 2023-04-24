package com.glia.widgets.filepreview.data.source.local;

import android.graphics.Bitmap;

import androidx.collection.ArrayMap;

public class InAppBitmapCache {

    private InAppBitmapCache() {
    }

    private final ArrayMap<String, Bitmap> bitmapsById = new ArrayMap<>();

    public static InAppBitmapCache getInstance() {
        return InstanceHolder.instance;
    }

    private static final class InstanceHolder {
        private static final InAppBitmapCache instance = new InAppBitmapCache();
    }

    public void putBitmap(String id, Bitmap bitmap) {
        bitmapsById.put(id, bitmap);
    }

    public Bitmap getBitmapById(String id) {
        return bitmapsById.get(id);
    }

    public void clear() {
        bitmapsById.clear();
    }
}
