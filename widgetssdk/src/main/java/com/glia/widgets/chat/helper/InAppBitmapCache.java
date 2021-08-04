package com.glia.widgets.chat.helper;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class InAppBitmapCache {

    private static InAppBitmapCache instance;

    private InAppBitmapCache() {
    }

    private final Map<String, Bitmap> bitmapsById = new HashMap<>();

    public static InAppBitmapCache getInstance() {
        if (instance == null) {
            synchronized (InAppBitmapCache.class) {
                if (instance == null) {
                    instance = new InAppBitmapCache();
                }
            }
        }

        return instance;
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
