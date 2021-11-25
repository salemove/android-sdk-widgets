package com.glia.widgets.core.configuration;

import com.glia.widgets.UiTheme;

public class GliaSdkConfiguration {
    private final String companyName;
    private final String queueId;
    private final String contextUrl;
    private final UiTheme runTimeTheme;
    private final boolean useOverlay;

    private GliaSdkConfiguration(
            Builder builder
    ) {
        this.companyName = builder.companyName;
        this.queueId = builder.queueId;
        this.contextUrl = builder.contextUrl;
        this.runTimeTheme = builder.runTimeTheme;
        this.useOverlay = builder.useOverlay;
    }
    
    public String getCompanyName() {
        return this.companyName;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public String getContextUrl() {
        return this.contextUrl;
    }

    public UiTheme getRunTimeTheme() {
        return this.runTimeTheme;
    }

    public boolean getUseOverlay() {
        return this.useOverlay;
    }

    public static class Builder {
        private String companyName;
        private String queueId;
        private String contextUrl;
        private UiTheme runTimeTheme;
        private boolean useOverlay;

        public Builder() {
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder queueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder contextUrl(String contextUrl) {
            this.contextUrl = contextUrl;
            return this;
        }

        public Builder runTimeTheme(UiTheme runTimeTheme) {
            this.runTimeTheme = runTimeTheme;
            return this;
        }

        public Builder useOverlay(boolean useOverlay) {
            this.useOverlay = useOverlay;
            return this;
        }

        public GliaSdkConfiguration build() {
            return new GliaSdkConfiguration(this);
        }
    }
}
