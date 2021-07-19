package com.glia.widgets.di;

import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.model.GliaScreenSharingRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;

public class RepositoryFactory {

    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private static GliaEngagementRepository gliaEngagementRepository;
    private static GliaVisitorMediaRepository gliaVisitorMediaRepository;
    private static GliaOperatorMediaRepository gliaOperatorMediaRepository;
    private static GliaQueueRepository gliaQueueRepository;
    private FileAttachmentRepository fileAttachmentRepository;

    public MediaUpgradeOfferRepository getMediaUpgradeOfferRepository() {
        if (mediaUpgradeOfferRepository == null) {
            mediaUpgradeOfferRepository = new MediaUpgradeOfferRepository();
        }
        return mediaUpgradeOfferRepository;
    }

    public GliaScreenSharingRepository getGliaScreenSharingRepository() {
        return new GliaScreenSharingRepository();
    }

    public GliaChatRepository getGliaMessageRepository() {
        return new GliaChatRepository();
    }

    public GliaEngagementRepository getGliaEngagementRepository() {
        if (gliaEngagementRepository == null) {
            gliaEngagementRepository = new GliaEngagementRepository();
        }
        return gliaEngagementRepository;
    }

    public GliaVisitorMediaRepository getGliaVisitorMediaRepository() {
        if (gliaVisitorMediaRepository == null) {
            gliaVisitorMediaRepository = new GliaVisitorMediaRepository();
        }
        return gliaVisitorMediaRepository;
    }

    public GliaOperatorMediaRepository getGliaOperatorMediaRepository() {
        if (gliaOperatorMediaRepository == null) {
            gliaOperatorMediaRepository = new GliaOperatorMediaRepository();
        }
        return gliaOperatorMediaRepository;
    }

    public GliaQueueRepository getGliaQueueRepository() {
        if (gliaQueueRepository == null) {
            gliaQueueRepository = new GliaQueueRepository();
        }
        return gliaQueueRepository;
    }

    public FileAttachmentRepository getGliaFileAttachmentRepository() {
        if (fileAttachmentRepository == null) {
            fileAttachmentRepository = new FileAttachmentRepository();
        }
        return fileAttachmentRepository;
    }
}
