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
    public void getInstance_returnsSame() {
        InAppBitmapCache result = cache = InAppBitmapCache.getInstance();
        assertEquals(cache, result);
    }

    @Test
    public void putBitmap_getBitmapById_returnsTheSameBitmap() {
        cache.putBitmap(ID, BITMAP);
        Bitmap result = cache.getBitmapById(ID);
        assertEquals(BITMAP, result);
    }

    @Test
    public void putBitmap_getBitmapById_returnsTheSameBitmap_whenBitmapUpdated() {
        cache.putBitmap(ID, BITMAP);
        cache.putBitmap(ID, BITMAP_2);
        Bitmap result = cache.getBitmapById(ID);
        assertEquals(BITMAP_2, result);
    }

    @Test
    public void putBitmap_getBitmapById_returnsNull_whenCleared() {
        cache.putBitmap(ID, BITMAP);
        cache.clear();
        Bitmap result = cache.getBitmapById(ID);
        assertNull(result);
    }

    @Test
    public void getBitmapById_returnsNull_whenCleared() {
        cache.clear();
        Bitmap result = cache.getBitmapById(ID);
        assertNull(result);
    }

    private static final String ID = "ID";
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private static final Bitmap BITMAP_2 = mock(Bitmap.class);
}
