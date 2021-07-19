package com.glia.widgets.chat.adapter;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class InAppFileCache {

    private static InAppFileCache instance;

    private InAppFileCache() {
    }

    private final Map<String, Bitmap> bitmapsById = new HashMap<>();

    public static InAppFileCache getInstance() {
        if (instance == null) {
            synchronized (InAppFileCache.class) {
                if (instance == null) {
                    instance = new InAppFileCache();
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
