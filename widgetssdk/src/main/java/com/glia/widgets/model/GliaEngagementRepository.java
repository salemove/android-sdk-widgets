package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

public class GliaEngagementRepository {
    private final String TAG = "GliaEngagementRepository";

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
}
