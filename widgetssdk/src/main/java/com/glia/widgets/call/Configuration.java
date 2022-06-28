package com.glia.widgets.call;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.helper.Utils;

public class Configuration {
    private final GliaSdkConfiguration sdkConfiguration;
    private final Engagement.MediaType mediaType;
    private final boolean isUpgradeToCall;

    private Configuration(Builder builder) {
        this.sdkConfiguration = builder.widgetsConfiguration;
        this.mediaType = builder.mediaType;
        this.isUpgradeToCall = builder.isUpgradeToCall != null ? builder.isUpgradeToCall : false;
    }

    public GliaSdkConfiguration getSdkConfiguration() {
        return this.sdkConfiguration;
    }

    public Engagement.MediaType getMediaType() {
        return this.mediaType;
    }

    public boolean getIsUpgradeToCall() {
        return this.isUpgradeToCall;
    }

    public static class Builder {
        private GliaSdkConfiguration widgetsConfiguration;
        private Engagement.MediaType mediaType;
        private Boolean isUpgradeToCall;

        public Builder setWidgetsConfiguration(GliaSdkConfiguration configuration) {
            this.widgetsConfiguration = configuration;
            return this;
        }

        public Builder setMediaType(Engagement.MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder setMediaType(String mediaType) {
            this.mediaType = Utils.toMediaType(mediaType);
            return this;
        }

        public Builder setIsUpgradeToCall(Boolean isUpgradeToCall) {
            this.isUpgradeToCall = isUpgradeToCall;
            return this;
        }

        public Builder() {
            this.isUpgradeToCall = false;
            this.mediaType = Engagement.MediaType.AUDIO;
        }

        public Configuration build() {
            return new Configuration(this);
        }

        public static Builder from(Configuration configuration) {
            return builder()
                    .setWidgetsConfiguration(configuration.sdkConfiguration)
                    .setMediaType(configuration.mediaType)
                    .setIsUpgradeToCall(configuration.isUpgradeToCall);
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}

