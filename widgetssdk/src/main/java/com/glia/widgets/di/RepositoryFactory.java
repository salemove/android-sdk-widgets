package com.glia.widgets.di;

import com.glia.widgets.chat.data.ChatScreenRepository;
import com.glia.widgets.chat.data.ChatScreenRepositoryImpl;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.chat.helper.FileHelper;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementStateRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.engagement.GliaOperatorRepository;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository;
import com.glia.widgets.core.secureconversations.SecureConversations;
import com.glia.widgets.core.secureconversations.SecureConversationsRepository;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.data.GliaFileRepositoryImpl;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;

public class RepositoryFactory {

    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private ChatScreenRepository chatScreenRepository;
    private static SecureConversationsRepository secureConversationsRepository;
    private static SecureFileAttachmentRepository secureFileAttachmentRepository;
    private static GliaEngagementRepository gliaEngagementRepository;
    private static GliaVisitorMediaRepository gliaVisitorMediaRepository;
    private static GliaOperatorMediaRepository gliaOperatorMediaRepository;
    private static GliaQueueRepository gliaQueueRepository;
    private static GliaFileRepository gliaFileRepository;
    private static GliaSurveyRepository gliaSurveyRepository;
    private static GliaEngagementTypeRepository gliaEngagementTypeRepository;
    private static GliaEngagementStateRepository gliaEngagementStateRepository;
    private static FileAttachmentRepository fileAttachmentRepository;
    private static GliaOperatorRepository operatorRepository;

    private final GliaCore gliaCore;
    private final DownloadsFolderDataSource downloadsFolderDataSource;
    private final FileHelper fileHelper = new FileHelper();

    public RepositoryFactory(
            GliaCore gliaCore,
            DownloadsFolderDataSource downloadsFolderDataSource
    ) {
        this.downloadsFolderDataSource = downloadsFolderDataSource;
        this.gliaCore = gliaCore;
    }

    public MediaUpgradeOfferRepository getMediaUpgradeOfferRepository() {
        if (mediaUpgradeOfferRepository == null) {
            mediaUpgradeOfferRepository = new MediaUpgradeOfferRepository();
        }
        return mediaUpgradeOfferRepository;
    }

    public GliaScreenSharingRepository getGliaScreenSharingRepository() {
        return new GliaScreenSharingRepository(gliaCore);
    }

    public GliaChatRepository getGliaMessageRepository() {
        return new GliaChatRepository();
    }

    public GliaEngagementRepository getGliaEngagementRepository() {
        if (gliaEngagementRepository == null) {
            gliaEngagementRepository = new GliaEngagementRepository(
                    gliaCore
            );
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
            gliaQueueRepository = new GliaQueueRepository(gliaCore);
        }
        return gliaQueueRepository;
    }

    public FileAttachmentRepository getGliaFileAttachmentRepository() {
        if (fileAttachmentRepository == null) {
            fileAttachmentRepository = new FileAttachmentRepository(gliaCore);
        }
        return fileAttachmentRepository;
    }

    public GliaFileRepository getGliaFileRepository() {
        if (gliaFileRepository == null) {
            gliaFileRepository = new GliaFileRepositoryImpl(
                    InAppBitmapCache.getInstance(),
                    downloadsFolderDataSource,
                    gliaCore,
                    fileHelper
            );
        }
        return gliaFileRepository;
    }

    public GliaSurveyRepository getGliaSurveyRepository() {
        if (gliaSurveyRepository == null) {
            gliaSurveyRepository = new GliaSurveyRepository(gliaCore);
        }
        return gliaSurveyRepository;
    }

    public GliaEngagementTypeRepository getGliaEngagementTypeRepository() {
        if (gliaEngagementTypeRepository == null) {
            gliaEngagementTypeRepository = new GliaEngagementTypeRepository(
                    getGliaEngagementRepository(),
                    getGliaVisitorMediaRepository(),
                    getGliaOperatorMediaRepository(),
                    getGliaEngagementStateRepository()
            );
        }
        return gliaEngagementTypeRepository;
    }

    public GliaEngagementStateRepository getGliaEngagementStateRepository() {
        if (gliaEngagementStateRepository == null) {
            gliaEngagementStateRepository = new GliaEngagementStateRepository(getOperatorRepository());
        }
        return gliaEngagementStateRepository;
    }

    public ChatScreenRepository getChatScreenRepository() {
        if (chatScreenRepository == null) {
            chatScreenRepository = new ChatScreenRepositoryImpl();
        }
        return chatScreenRepository;
    }

    public GliaOperatorRepository getOperatorRepository() {
        if (operatorRepository == null) {
            operatorRepository = new GliaOperatorRepository(gliaCore);
        }

        return operatorRepository;
    }

    public SecureConversationsRepository getSecureConversationsRepository() {
        if (secureConversationsRepository == null) {
            SecureConversations secureConversations = new GliaCoreImpl().getSecureConversations();
            secureConversationsRepository = new SecureConversationsRepository(secureConversations);
        }
        return secureConversationsRepository;
    }

    public SecureFileAttachmentRepository getSecureFileAttachmentRepository() {
        if (secureFileAttachmentRepository == null) {
            SecureConversations secureConversations = new GliaCoreImpl().getSecureConversations();
            secureFileAttachmentRepository = new SecureFileAttachmentRepository(secureConversations);
        }
        return secureFileAttachmentRepository;
    }
}
