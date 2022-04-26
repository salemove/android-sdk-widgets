package com.glia.widgets.core.engagement;

import androidx.annotation.NonNull;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.site.SiteInfo;
import com.glia.widgets.di.GliaCore;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaEngagementRepository {
    private final String TAG = GliaEngagementRepository.class.getSimpleName();
    private final GliaCore gliaCore;

    public GliaEngagementRepository(
            GliaCore gliaCore
    ) {
        this.gliaCore = gliaCore;
    }

    public void listenForEngagementEnd(OmnicoreEngagement engagement, Runnable engagementEnded) {
        engagement.on(Engagement.Events.END, engagementEnded);
    }

    public void unregisterEngagementEndListener(Runnable engagementEnded) {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> {
            engagement.off(Engagement.Events.END, engagementEnded);
        });
    }

    public void endEngagement() {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> engagement.end(e -> {
            if (e != null) {
                Logger.e(TAG, "Ending engagement error: " + e);
            }
        }));
    }

    public void listenForEngagement(Consumer<OmnicoreEngagement> engagementConsumer) {
        gliaCore.on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void unregisterEngagementListener(Consumer<OmnicoreEngagement> engagementConsumer) {
        gliaCore.off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public boolean hasOngoingEngagement() {
        return gliaCore.getCurrentEngagement().isPresent();
    }

    public void getSiteInfo(@NonNull RequestCallback<SiteInfo> callback) {
        gliaCore.getSiteInfo(callback);
    }
}
