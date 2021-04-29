package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

public class GliaEngagementRepository {
    private final String TAG = "GliaEngagementRepository";

    public interface EngagementListener {
        void success(OmnicoreEngagement engagement);
    }

    public interface EngagementEndListener {
        void engagementEnded();
    }

    public void listenForEngagement(EngagementListener listener) {
        Glia.on(Glia.Events.ENGAGEMENT, listener::success);
    }

    public void unregisterEngagementListener(EngagementListener listener) {
        Glia.off(Glia.Events.ENGAGEMENT, listener::success);
    }

    public void listenForEngagementEnd(OmnicoreEngagement engagement, EngagementEndListener engagementEndListener) {
        engagement.on(Engagement.Events.END, engagementEndListener::engagementEnded);
    }

    public void unregisterEngagementEndListener(EngagementEndListener engagementEndListener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.off(Engagement.Events.END, engagementEndListener::engagementEnded);
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
}
