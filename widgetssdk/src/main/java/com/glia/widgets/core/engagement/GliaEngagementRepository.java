package com.glia.widgets.core.engagement;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaEngagementRepository {
    private final String TAG = GliaEngagementRepository.class.getSimpleName();

    enum EngagementType {NONE, CHAT, MEDIA}

    private EngagementType engagementType = EngagementType.NONE;

    public GliaEngagementRepository() { }

    private void setEngagementType(EngagementType type) {
        engagementType = type;
    }

    public void clearEngagementType() {
        setEngagementType(EngagementType.NONE);
    }

    public void onChatEngagement() {
        setEngagementType(EngagementType.CHAT);
    }

    public void onMediaEngagement() {
        setEngagementType(EngagementType.MEDIA);
    }

    public void onUpgradeToMediaEngagement() {
        setEngagementType(EngagementType.MEDIA);
    }

    public boolean isMediaEngagement() {
        return engagementType == EngagementType.MEDIA;
    }

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
