package com.glia.widgets;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.Manifest;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.glia.widgets.call.CallActivity;
import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallStateHelper;
import com.glia.widgets.call.CallStatus;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.di.ControllerFactory;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CallActivityTest {
    private Context appContext;
    private ControllerFactory controllerFactory;
    private CallController callController;
    private CallViewCallback callViewCallback;
    private ServiceChatHeadController serviceChatHeadController;
    private GliaSdkConfigurationManager sdkConfigurationManager;
    private ResourceProvider resourceProvider;
    private FloatingVisitorVideoContract.Controller floatingVisitorVideoController;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // set up ControllerFactory
        controllerFactory = mock(ControllerFactory.class);
        callController = mock(CallController.class);
        serviceChatHeadController = mock(ServiceChatHeadController.class);
        floatingVisitorVideoController = mock(FloatingVisitorVideoContract.Controller.class);
        doAnswer(invocation -> {
            callViewCallback = invocation.getArgument(0);
            return callController;
        }).when(controllerFactory).getCallController(any());
        when(controllerFactory.getChatHeadController())
                .thenReturn(serviceChatHeadController);
        when(controllerFactory.getFloatingVisitorVideoController())
                .thenReturn(floatingVisitorVideoController);
        Dependencies.setControllerFactory(controllerFactory);

        // set up SdkConfigurationManager
        sdkConfigurationManager = mock(GliaSdkConfigurationManager.class);
        Dependencies.setSdkConfigurationManager(sdkConfigurationManager);

        // set up ResourceProvider
        resourceProvider = new ResourceProvider(appContext);
        Dependencies.setResourceProvider(resourceProvider);

        // set up StringProvider
        StringProvider stringProvider = new AndroidTestStringProvider(appContext);
        Dependencies.setStringProvider(stringProvider);
    }

    @Test
    public void testCallViewInvisible() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(false)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            onView(withId(R.id.call_view)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void testCallViewVisible() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            onView(withId(R.id.call_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testMinimizeButtonContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.engagement_minimize_video_button);
            onView(withId(R.id.minimize_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testSpeakerButtonOnContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setIsSpeakerOn(true)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.android_call_turn_speaker_off_button_accessibility);
            onView(withId(R.id.speaker_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testSpeakerButtonOffContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setIsSpeakerOn(false)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.android_call_turn_speaker_on_button_accessibility);
            onView(withId(R.id.speaker_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testMuteButtonContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setIsMuted(true)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.glia_call_mute_content_description);
            onView(withId(R.id.mute_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testUnmuteButtonContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setIsMuted(false)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.glia_call_unmute_content_description);
            onView(withId(R.id.mute_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testVideoButtonOnContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setHasVideo(true)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.android_call_turn_video_off_button_accessibility);
            onView(withId(R.id.video_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testVideoButtonOffContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setHasVideo(false)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.android_call_turn_video_on_button_accessibility);
            onView(withId(R.id.video_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testChatButtonZeroMessagesContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setMessagesNotSeen(0)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.glia_call_chat_zero_content_description);
            onView(withId(R.id.chat_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testChatButtonPluralsMessagesContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .setMessagesNotSeen(15)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getResources().getString(R.string.glia_call_chat_other_content_description, 15);
            onView(withId(R.id.chat_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testConnectingViewContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.engagement_connection_screen_connect_with, "FormattedOperatorName", "");
            onView(withId(R.id.connecting_view)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testVisitorVideoContainerContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.call_visitor_video_accessibility_label);
            onView(withId(R.id.floating_visitor_video)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testOperatorVideoContainerContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.call_operator_video_accessibility_label);
            onView(withId(R.id.operator_video_container)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testEndButtonContentDescription() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.glia_top_app_bar_chat_end_content_description);
            onView(withId(R.id.end_button)).check(matches(withContentDescription(expected)));
        }
    }

    @Test
    public void testNavigateUpButtonContentDescription() throws InterruptedException {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> scenario = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            Thread.sleep(1000);

            onView(withContentDescription(R.string.android_app_bar_nav_up_accessibility)).perform(click());

            assertEquals(Lifecycle.State.DESTROYED, scenario.getState());
        }
    }

    @Test
    public void testOperatorNameHint() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.glia_call_operator_name_hint);
            onView(withId(R.id.operator_name_view)).check(matches(withHint(expected)));
        }
    }

    @Test
    public void testCallDurationHint() {
        when(callController.shouldShowMediaEngagementView(anyBoolean())).thenReturn(true);

        try (ActivityScenario<CallActivity> ignored = launch(CallActivity.class)) {
            CallStatus callStatus = mock(CallStatus.class);
            when(callStatus.getFormattedOperatorName()).thenReturn("FormattedOperatorName");
            CallStateHelper callState = new CallStateHelper.Builder()
                    .setVisible(true)
                    .setCallStatus(callStatus)
                    .build();

            callViewCallback.emitState(callState.makeCallState());

            String expected = appContext.getString(R.string.call_duration_accessibility_label);
            onView(withId(R.id.call_timer_view)).check(matches(withHint(expected)));
        }
    }
}