package com.glia.widgets.core.engagement;

import androidx.annotation.NonNull;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.omnibrowse.Omnibrowse;
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.site.SiteInfo;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaEngagementRepository {
    private final String TAG = GliaEngagementRepository.class.getSimpleName();
    private final GliaCore gliaCore;

    public GliaEngagementRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void listenForEngagementEnd(OmnicoreEngagement engagement, Runnable engagementEnded) {
        engagement.on(Engagement.Events.END, engagementEnded);
    }

    public void listenForEngagementEnd(OmnibrowseEngagement engagement, Runnable engagementEnded) {
        engagement.on(Engagement.Events.END, engagementEnded);
    }

    public void unregisterEngagementEndListener(Runnable engagementEnded) {
        // Engagement#off(Event, Callback) does not support `null` callback
        if (engagementEnded == null) {
            return;
        }
        gliaCore.getCurrentEngagement().ifPresent(
                engagement -> engagement.off(Engagement.Events.END, engagementEnded));
    }

    public void endEngagement() {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> engagement.end(e -> {
            if (e != null) {
                Logger.e(TAG, "Ending engagement error: " + e);
            }
        }));
    }

    public void listenForOmnicoreEngagement(Consumer<OmnicoreEngagement> engagementConsumer) {
        gliaCore.on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void listenForCallVisualizerEngagement(Consumer<OmnibrowseEngagement> engagementConsumer) {
        gliaCore.getCallVisualizer().on(Omnibrowse.Events.ENGAGEMENT, engagementConsumer);
    }

    public void listenForOmnibrowseEngagement(Consumer<OmnibrowseEngagement> engagementConsumer) {
        gliaCore.getCallVisualizer().on(Omnibrowse.Events.ENGAGEMENT, engagementConsumer);
    }

    public void unregisterEngagementListener(Consumer<OmnicoreEngagement> engagementConsumer) {
        gliaCore.off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void unregisterCallVisualizerEngagementListener(Consumer<OmnibrowseEngagement> engagementConsumer) {
        gliaCore.getCallVisualizer().off(Omnibrowse.Events.ENGAGEMENT, engagementConsumer);
    }

    public boolean hasOngoingEngagement() {
        return gliaCore.getCurrentEngagement().isPresent();
    }

    public boolean isCallVisualizerEngagement() {
        return gliaCore.getCurrentEngagement()
                .filter(engagement -> engagement instanceof OmnibrowseEngagement)
                .isPresent();
    }

    public void getSiteInfo(@NonNull RequestCallback<SiteInfo> callback) {
        gliaCore.getSiteInfo(callback);
    }
}
