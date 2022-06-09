package com.glia.widgets;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class DownloadsFolderDataSourceTest {

    private Context appContext;
    private DownloadsFolderDataSource dataSource;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dataSource = new DownloadsFolderDataSource(appContext);
    }

    @Test
    public void putImageToDownloads_returnsCompletableCompletes() {
        dataSource.putImageToDownloads(IMAGE_NAME, BITMAP)
                .test()
                .assertComplete();
    }

    @Test
    public void getImageFromDownloadsFolder_returnsMaybeOnError() {
        dataSource.getImageFromDownloadsFolder(IMAGE_NAME)
                .test()
                .assertError(FileNotFoundException.class);
    }

    @Test
    public void downloadFileToDownloads_returnsCompletableCompletes() {
        dataSource.downloadFileToDownloads(
                        IMAGE_NAME,
                        "content_type",
                        INPUT_STREAM
                )
                .test()
                .assertComplete();
    }

    private static final String IMAGE_NAME = "IMAGE_NAME";
    private static final Bitmap BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
    private static final InputStream INPUT_STREAM = new ByteArrayInputStream("test data".getBytes());
}
