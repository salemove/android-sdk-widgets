package com.glia.widgets.chat.domain;

import androidx.annotation.NonNull;

import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.site.SiteInfo;
import com.glia.widgets.core.engagement.GliaEngagementRepository;

public class SiteInfoUseCase {
    private final GliaEngagementRepository repository;

    public SiteInfoUseCase(GliaEngagementRepository repository) {
        this.repository = repository;
    }

    public void execute(@NonNull RequestCallback<SiteInfo> callback) {
        repository.getSiteInfo(callback);
    }

}
