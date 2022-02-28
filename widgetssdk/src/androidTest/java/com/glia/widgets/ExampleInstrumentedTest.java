package com.glia.widgets;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.di.ControllerFactory;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.ui.FilePreviewActivity;
import com.glia.widgets.filepreview.ui.FilePreviewController;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.glia.widgets.test", appContext.getPackageName());
    }

    // just example
    @Test
    public void testFilePreview() throws InterruptedException {
        FilePreviewController filePreviewController = mock(FilePreviewController.class);
        ControllerFactory controllerFactory = mock(ControllerFactory.class);
        when(controllerFactory.getImagePreviewController()).thenReturn(filePreviewController);
        Dependencies.setControllerFactory(controllerFactory);

        ActivityScenario<FilePreviewActivity> scenario = launch(FilePreviewActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        Thread.sleep(10); // sleep if needs

        onView(withId(R.id.file_preview_view)).check(matches(isDisplayed()));
    }
}
