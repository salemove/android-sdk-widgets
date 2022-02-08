package com.glia.widgets.core.mediaupgradeoffer;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MediaUpgradeOfferRepository {
    private final static String TAG = "MediaUpgradeOfferRepository";

    private final List<MediaUpgradeOfferRepositoryCallback> callbacks = new ArrayList<>();
    private final Consumer<MediaUpgradeOffer> upgradeOfferConsumer = offer -> {
        Logger.d(TAG, "upgradeOfferConsumer, offer: " + offer.toString());
        for (MediaUpgradeOfferRepositoryCallback callback : callbacks) {
            callback.newOffer(offer);
        }
    };
    private final Consumer<OmnicoreEngagement> engagementHandler = engagement ->
            engagement.getMedia().on(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer);

    public void startListening() {
        Dependencies.glia().on(Glia.Events.ENGAGEMENT, engagementHandler);
    }

    public void addCallback(MediaUpgradeOfferRepositoryCallback callback) {
        Logger.d(TAG, "addCallback");
        callbacks.add(callback);
    }

    public void acceptOffer(MediaUpgradeOffer offer, Submitter submitter) {
        offer.accept(exception -> {
            if (exception == null) {
                Logger.d(TAG, "acceptOfferSuccess");
                for (MediaUpgradeOfferRepositoryCallback callback : callbacks) {
                    callback.upgradeOfferChoiceSubmitSuccess(offer, submitter);
                }
            } else {
                Logger.d(TAG, "acceptOfferFailed");
            }
        });
    }

    public void declineOffer(MediaUpgradeOffer offer, Submitter submitter) {
        offer.decline(exception -> {
            if (exception == null) {
                Logger.d(TAG, "declineOfferSuccess");
                for (MediaUpgradeOfferRepositoryCallback callback : callbacks) {
                    callback.upgradeOfferChoiceDeclinedSuccess(submitter);
                }
            } else {
                Logger.d(TAG, "declineOfferFailed");
            }
        });
    }

    public void stopAll() {
        Logger.d(TAG, "stopAll");
        callbacks.clear();
        Dependencies.glia().off(Glia.Events.ENGAGEMENT, engagementHandler);
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getMedia().off(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer));
    }

    public enum Submitter {
        CHAT, CALL
    }
}
