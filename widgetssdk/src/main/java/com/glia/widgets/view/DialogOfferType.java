package com.glia.widgets.view;

import com.glia.androidsdk.comms.MediaUpgradeOffer;

public interface DialogOfferType {

    String getOperatorName();

    MediaUpgradeOffer getUpgradeOffer();

    class AudioUpgradeOffer implements DialogOfferType {
        private final MediaUpgradeOffer mediaUpgradeOffer;
        private final String operatorName;

        public AudioUpgradeOffer(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
            this.mediaUpgradeOffer = mediaUpgradeOffer;
            this.operatorName = operatorName;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public MediaUpgradeOffer getUpgradeOffer() {
            return mediaUpgradeOffer;
        }
    }

    class VideoUpgradeOffer1Way implements DialogOfferType {
        private final MediaUpgradeOffer mediaUpgradeOffer;
        private final String operatorName;

        public VideoUpgradeOffer1Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
            this.mediaUpgradeOffer = mediaUpgradeOffer;
            this.operatorName = operatorName;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public MediaUpgradeOffer getUpgradeOffer() {
            return mediaUpgradeOffer;
        }
    }

    class VideoUpgradeOffer2Way implements DialogOfferType {
        private final MediaUpgradeOffer mediaUpgradeOffer;
        private final String operatorName;

        public VideoUpgradeOffer2Way(MediaUpgradeOffer mediaUpgradeOffer, String operatorName) {
            this.mediaUpgradeOffer = mediaUpgradeOffer;
            this.operatorName = operatorName;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public MediaUpgradeOffer getUpgradeOffer() {
            return mediaUpgradeOffer;
        }
    }
}
