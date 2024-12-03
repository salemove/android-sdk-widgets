package com.glia.widgets.di;

import com.glia.widgets.chat.data.ChatScreenRepository;
import com.glia.widgets.chat.data.ChatScreenRepositoryImpl;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository;
import com.glia.widgets.core.engagement.GliaOperatorRepository;
import com.glia.widgets.core.engagement.GliaOperatorRepositoryImpl;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository;
import com.glia.widgets.core.queue.QueueRepository;
import com.glia.widgets.core.queue.QueueRepositoryImpl;
import com.glia.widgets.core.secureconversations.SecureConversationsRepository;
import com.glia.widgets.core.secureconversations.SendMessageRepository;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.engagement.EngagementRepository;
import com.glia.widgets.engagement.EngagementRepositoryImpl;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.data.GliaFileRepositoryImpl;
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource;
import com.glia.widgets.filepreview.data.source.local.InAppBitmapCache;
import com.glia.widgets.launcher.ConfigurationManager;
import com.glia.widgets.permissions.PermissionsRequestRepository;

/**
 * @hide
 */
public class RepositoryFactory {
    private static SecureConversationsRepository secureConversationsRepository;
    private static SecureFileAttachmentRepository secureFileAttachmentRepository;
    private static QueueRepository queueRepository;
    private static GliaFileRepository gliaFileRepository;
    private static FileAttachmentRepository fileAttachmentRepository;
    private static GliaOperatorRepository operatorRepository;
    private static SendMessageRepository sendMessageRepository;
    private static VisitorCodeRepository visitorCodeRepository;
    private static PermissionsRequestRepository permissionsRequestRepository;
    private final GliaCore gliaCore;
    private final DownloadsFolderDataSource downloadsFolderDataSource;
    private final ConfigurationManager configurationManager;
    private ChatScreenRepository chatScreenRepository;
    private EngagementRepository engagementRepository;

    public RepositoryFactory(
        GliaCore gliaCore,
        DownloadsFolderDataSource downloadsFolderDataSource,
        ConfigurationManager configurationManager
    ) {
        this.downloadsFolderDataSource = downloadsFolderDataSource;
        this.gliaCore = gliaCore;
        this.configurationManager = configurationManager;
    }

    public void initialize() {
        getEngagementRepository().initialize();
        getQueueRepository().initialize();
        getSecureConversationsRepository().initialize();
    }

    public GliaChatRepository getGliaMessageRepository() {
        return new GliaChatRepository(gliaCore);
    }

    public QueueRepository getQueueRepository() {
        if (queueRepository == null) {
            queueRepository = new QueueRepositoryImpl(gliaCore, configurationManager);
        }
        return queueRepository;
    }

    public FileAttachmentRepository getGliaFileAttachmentRepository() {
        if (fileAttachmentRepository == null) {
            fileAttachmentRepository = new FileAttachmentRepository(gliaCore);
        }
        return fileAttachmentRepository;
    }

    public GliaFileRepository getGliaFileRepository() {
        if (gliaFileRepository == null) {
            gliaFileRepository = new GliaFileRepositoryImpl(InAppBitmapCache.getInstance(), downloadsFolderDataSource, gliaCore);
        }
        return gliaFileRepository;
    }

    public GliaSurveyRepository getGliaSurveyRepository() {
        return new GliaSurveyRepository(gliaCore);
    }

    public ChatScreenRepository getChatScreenRepository() {
        if (chatScreenRepository == null) {
            chatScreenRepository = new ChatScreenRepositoryImpl();
        }
        return chatScreenRepository;
    }

    public GliaOperatorRepository getOperatorRepository() {
        if (operatorRepository == null) {
            operatorRepository = new GliaOperatorRepositoryImpl(gliaCore);
        }

        return operatorRepository;
    }

    public VisitorCodeRepository getVisitorCodeRepository() {
        if (visitorCodeRepository == null) {
            visitorCodeRepository = new VisitorCodeRepository(gliaCore);
        }
        return visitorCodeRepository;
    }

    public SecureConversationsRepository getSecureConversationsRepository() {
        if (secureConversationsRepository == null) {
            secureConversationsRepository = new SecureConversationsRepository(gliaCore, getQueueRepository());
        }
        return secureConversationsRepository;
    }

    public SecureFileAttachmentRepository getSecureFileAttachmentRepository() {
        if (secureFileAttachmentRepository == null) {
            secureFileAttachmentRepository = new SecureFileAttachmentRepository(gliaCore);
        }
        return secureFileAttachmentRepository;
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

    public EngagementRepository getEngagementRepository() {
        if (engagementRepository == null) {
            engagementRepository = new EngagementRepositoryImpl(
                gliaCore,
                getOperatorRepository(),
                getQueueRepository(),
                configurationManager
            );
        }
        return engagementRepository;
    }

}
