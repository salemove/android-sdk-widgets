package com.glia.widgets.filepreview.data.source.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;

public class InAppBitmapCacheTest {

    private InAppBitmapCache cache;

    @Before
    public void setUp() {
        cache = InAppBitmapCache.getInstance();
    }

    @Test
    public void getInstance_returnsSameInstance_whenCalledMoreThanOnce() {
        InAppBitmapCache result = InAppBitmapCache.getInstance();
        assertEquals(cache, result);
    }

    @Test
    public void putBitmap_successful_whenValidArguments() {
        cache.putBitmap(ID, BITMAP);
    }

    @Test
    public void putBitmap_successful_whenIdArgumentNull() {
        cache.putBitmap(null, BITMAP);
    }

    @Test
    public void putBitmap_successful_whenBitmapArgumentNull() {
        cache.putBitmap(ID, null);
    }

    @Test
    public void putBitmap_successful_whenArgumentsNull() {
        cache.putBitmap(null, null);
    }

    @Test
    public void getBitmapById_returnsBitmap_whenBitmapInCache() {
        cache.clear();
        cache.putBitmap(ID, BITMAP);
        Bitmap result = cache.getBitmapById(ID);
        assertEquals(BITMAP, result);
    }

    @Test
    public void getBitmapById_returnsBitmap_whenBitmapUpdated() {
        cache.clear();
        cache.putBitmap(ID, BITMAP);
        cache.putBitmap(ID, BITMAP_2);
        Bitmap result = cache.getBitmapById(ID);
        assertEquals(BITMAP_2, result);
    }

    @Test
    public void getBitmapById_returnsNull_whenEmpty() {
        cache.clear();
        Bitmap result = cache.getBitmapById(ID);
        assertNull(result);
    }

    @Test
    public void getBitmapById_returnsNull_whenCleared() {
        cache.clear();
        cache.putBitmap(ID, BITMAP);
        cache.clear();
        Bitmap result = cache.getBitmapById(ID);
        assertNull(result);
    }

    @Test
    public void getBitmapById_returnsNull_whenIdArgumentNull() {
        cache.clear();
        Bitmap result = cache.getBitmapById(null);
        assertNull(result);
    }

    private static final String ID = "ID";
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private static final Bitmap BITMAP_2 = mock(Bitmap.class);
}
