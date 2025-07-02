package com.glia.widgets;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(AndroidJUnit4.class)
public class DownloadsFolderDataSourceTest {

    private DownloadsFolderDataSource dataSource;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
        .grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dataSource = new DownloadsFolderDataSource(appContext);
    }

    @Test
    public void putImageToDownloads_completesSuccessfully_whenValidArguments() {
        dataSource.putImageToDownloads(IMAGE_NAME, BITMAP)
            .test()
            .assertComplete();
    }

    @Test
    public void putImageToDownloads_emitsNullPointerException_whenNullImageNameArgument() {
        dataSource.putImageToDownloads(null, BITMAP)
            .test()
            .assertError(NullPointerException.class);
    }

    @Test
    public void putImageToDownloads_emitsNullPointerException_whenNullBitmapArgument() {
        dataSource.putImageToDownloads(IMAGE_NAME, null)
            .test()
            .assertError(NullPointerException.class);
    }

    @Test
    public void putImageToDownloads_emitsNullPointerException_whenNullArguments() {
        dataSource.putImageToDownloads(null, null)
            .test()
            .assertError(NullPointerException.class);
    }

    @Test
    public void getImageFromDownloadsFolder_emitsFileNotFoundException_whenImageNotInDownloadsFolder() {
        dataSource.getImageFromDownloadsFolder(IMAGE_NAME)
            .test()
            .assertError(FileNotFoundException.class);
    }

    @Test
    public void getImageFromDownloadsFolder_emitsNullPointerException_whenNullArgument() {
        dataSource.getImageFromDownloadsFolder(null)
            .test()
            .assertError(NullPointerException.class);
    }

    @Test
    public void downloadFileToDownloads_completesSuccessfully_whenValidArguments() {
        dataSource.downloadFileToDownloads(
                IMAGE_NAME,
                "content_type",
                INPUT_STREAM
            )
            .test()
            .assertComplete();
    }

    @Test
    public void downloadFileToDownloads_emitsNullPointerException_whenNullFileNameArgument() {
        dataSource.downloadFileToDownloads(
                null,
                "content_type",
                INPUT_STREAM
            )
            .test()
            .assertError(NullPointerException.class);
    }

    @Test
    public void downloadFileToDownloads_completesSuccessfully_whenNullContentTypeArgument() {
        dataSource.downloadFileToDownloads(
                IMAGE_NAME,
                null,
                INPUT_STREAM
            )
            .test()
            .assertComplete();
    }

    @Test
    public void downloadFileToDownloads_emitsNullPointerException_whenNullInputStreamArgument() {
        dataSource.downloadFileToDownloads(
                IMAGE_NAME,
                "content_type",
                null
            )
            .test()
            .assertError(NullPointerException.class);
    }

    private static final String IMAGE_NAME = "IMAGE_NAME";
    private static final Bitmap BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
    private static final InputStream INPUT_STREAM = new ByteArrayInputStream("test data".getBytes());
}
