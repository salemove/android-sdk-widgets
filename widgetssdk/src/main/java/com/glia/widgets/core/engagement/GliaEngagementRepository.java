package com.glia.widgets.core.engagement;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaEngagementRepository {
    private final String TAG = GliaEngagementRepository.class.getSimpleName();

    public void listenForEngagementEnd(OmnicoreEngagement engagement, Runnable engagementEnded) {
        engagement.on(Engagement.Events.END, engagementEnded);
    }

    public void unregisterEngagementEndListener(Runnable engagementEnded) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.off(Engagement.Events.END, engagementEnded);
        });
    }

    public void endEngagement() {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.end(e -> {
                if (e != null) {
                    Logger.e(TAG, "Ending engagement error: " + e.toString());
                }
            });
        });
    }

    public void listenForEngagement(Consumer<OmnicoreEngagement> engagementConsumer) {
        Glia.on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void unregisterEngagementListener(Consumer<OmnicoreEngagement> engagementConsumer) {
        Glia.off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public boolean hasOngoingEngagement() {
        return Glia.getCurrentEngagement().isPresent();
    }
}
