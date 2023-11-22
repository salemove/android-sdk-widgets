package com.glia.widgets.di;

import com.glia.widgets.callvisualizer.CallVisualizerRepository;
import com.glia.widgets.chat.data.ChatScreenRepository;
import com.glia.widgets.chat.data.ChatScreenRepositoryImpl;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository;
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository;
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
import com.glia.widgets.core.secureconversations.SecureConversationsRepository;
import com.glia.widgets.core.secureconversations.SendMessageRepository;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;
import com.glia.widgets.engagement.EngagementDataSourceImpl;
import com.glia.widgets.engagement.SurveyDataSourceImpl;
import com.glia.widgets.engagement.end.EngagementEndRepository;
import com.glia.widgets.engagement.end.EngagementEndRepositoryImpl;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.data.GliaFileRepositoryImpl;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.permissions.PermissionsRequestRepository;

public class RepositoryFactory {

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
    private static GliaEngagementConfigRepository engagementConfigRepository;
    private static SendMessageRepository sendMessageRepository;
    private static VisitorCodeRepository visitorCodeRepository;
    private static PermissionsRequestRepository permissionsRequestRepository;
    private final GliaCore gliaCore;
    private final DownloadsFolderDataSource downloadsFolderDataSource;
    private MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private GliaScreenSharingRepository gliaScreenSharingRepository;
    private ChatScreenRepository chatScreenRepository;
    private CallVisualizerRepository callVisualizerRepository;

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
        if (gliaScreenSharingRepository == null) {
            gliaScreenSharingRepository = new GliaScreenSharingRepository(gliaCore);
        }
        return gliaScreenSharingRepository;
    }

    public GliaChatRepository getGliaMessageRepository() {
        return new GliaChatRepository(gliaCore);
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
            fileAttachmentRepository = new FileAttachmentRepository(
                gliaCore,
                getEngagementConfigRepository()
            );
        }
        return fileAttachmentRepository;
    }

    public GliaFileRepository getGliaFileRepository() {
        if (gliaFileRepository == null) {
            gliaFileRepository = new GliaFileRepositoryImpl(
                InAppBitmapCache.getInstance(),
                downloadsFolderDataSource,
                gliaCore,
                getEngagementConfigRepository()
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

    public CallVisualizerRepository getCallVisualizerRepository() {
        if (callVisualizerRepository == null) {
            callVisualizerRepository = new CallVisualizerRepository(gliaCore);
        }

        return callVisualizerRepository;
    }

    public VisitorCodeRepository getVisitorCodeRepository() {
        if (visitorCodeRepository == null) {
            visitorCodeRepository = new VisitorCodeRepository(gliaCore);
        }
        return visitorCodeRepository;
    }

    public SecureConversationsRepository getSecureConversationsRepository() {
        if (secureConversationsRepository == null) {
            secureConversationsRepository = new SecureConversationsRepository(gliaCore.getSecureConversations());
        }
        return secureConversationsRepository;
    }

    public SecureFileAttachmentRepository getSecureFileAttachmentRepository() {
        if (secureFileAttachmentRepository == null) {
            secureFileAttachmentRepository = new SecureFileAttachmentRepository(gliaCore);
        }
        return secureFileAttachmentRepository;
    }

    public GliaEngagementConfigRepository getEngagementConfigRepository() {
        if (engagementConfigRepository == null) {
            engagementConfigRepository = new GliaEngagementConfigRepository();
        }
        return engagementConfigRepository;
    }

    public SendMessageRepository getSendMessageRepository() {
        if (sendMessageRepository == null) {
            sendMessageRepository = new SendMessageRepository();
        }
        return sendMessageRepository;
    }

    public PermissionsRequestRepository getPermissionsRequestRepository() {
        if (permissionsRequestRepository == null) {
            permissionsRequestRepository = new PermissionsRequestRepository();
        }
        return permissionsRequestRepository;
    }

    public EngagementEndRepository getEngagementEndRepository() {
        return new EngagementEndRepositoryImpl(
            new EngagementDataSourceImpl(gliaCore),
            new SurveyDataSourceImpl()
        );
    }
}
