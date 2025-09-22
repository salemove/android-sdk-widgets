package com.glia.widgets.di

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.glia.androidsdk.GliaConfig
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.internal.logger.TelemetryHelper
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.GliaTelemetry
import com.glia.telemetry_lib.GlobalAttribute
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.BuildConfig
import com.glia.widgets.GliaWidgets
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callvisualizer.CallVisualizerActivityWatcher
import com.glia.widgets.engagement.completion.EngagementCompletionActivityWatcher
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.entrywidget.EntryWidgetImpl
import com.glia.widgets.fcm.PushNotifications
import com.glia.widgets.fcm.PushNotificationsImpl
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.DeviceMonitor
import com.glia.widgets.helper.GliaActivityManagerImpl
import com.glia.widgets.helper.IntentHelperImpl
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.internal.audio.AudioControlManager
import com.glia.widgets.internal.audio.domain.OnAudioStartedUseCase
import com.glia.widgets.internal.authentication.AuthenticationManager
import com.glia.widgets.internal.authentication.toCoreType
import com.glia.widgets.internal.callvisualizer.CallVisualizerManager
import com.glia.widgets.internal.chathead.ChatHeadManager
import com.glia.widgets.internal.dialog.PermissionDialogManager
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.notification.device.NotificationManager
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.launcher.ActivityLauncherImpl
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.launcher.ConfigurationManagerImpl
import com.glia.widgets.launcher.EngagementLauncher
import com.glia.widgets.launcher.EngagementLauncherImpl
import com.glia.widgets.liveobservation.LiveObservation
import com.glia.widgets.liveobservation.LiveObservationImpl
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.operator.OperatorRequestActivityWatcher
import com.glia.widgets.permissions.ActivityWatcherForPermissionsRequest
import com.glia.widgets.secureconversations.SecureConversations
import com.glia.widgets.secureconversations.SecureConversationsImpl
import com.glia.widgets.toCoreType
import com.glia.widgets.view.dialog.UiComponentsActivityWatcher
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import com.glia.widgets.view.dialog.UiComponentsDispatcherImpl
import com.glia.widgets.view.head.ActivityWatcherForChatHead
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.snackbar.liveobservation.ActivityWatcherForLiveObservation
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager


internal object Dependencies {
    @JvmStatic
    val gliaThemeManager = UnifiedThemeManager()
    private val authenticationManagerProvider = AuthenticationManagerProvider()

    @JvmStatic
    lateinit var controllerFactory: ControllerFactory
        @VisibleForTesting set
    private lateinit var notificationManager: INotificationManager

    @JvmStatic
    lateinit var callVisualizerManager: CallVisualizerManager
        private set

    @JvmStatic
    lateinit var useCaseFactory: UseCaseFactory
        @VisibleForTesting set

    private lateinit var managerFactory: ManagerFactory

    var gliaCore: GliaCore = GliaCoreImpl()
        @VisibleForTesting set

    @JvmStatic
    lateinit var resourceProvider: ResourceProvider
        @VisibleForTesting set
    lateinit var localeProvider: LocaleProvider
        @VisibleForTesting set
    var schedulers: Schedulers = GliaWidgetsSchedulers()
        @VisibleForTesting set

    @JvmStatic
    val configurationManager: ConfigurationManager by lazy { ConfigurationManagerImpl() }

    @JvmStatic
    val activityLauncher: ActivityLauncher by lazy {
        ActivityLauncherImpl(IntentHelperImpl(), repositoryFactory.engagementRepository)
    }

    @JvmStatic
    val engagementLauncher: EngagementLauncher by lazy {
        EngagementLauncherImpl(
            activityLauncher = activityLauncher,
            configurationManager = configurationManager,
            uiComponentsDispatcher = uiComponentsDispatcher,
            hasOngoingSecureConversationUseCase = useCaseFactory.hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase = useCaseFactory.isQueueingOrEngagementUseCase,
            engagementTypeUseCase = useCaseFactory.engagementTypeUseCase
        )
    }

    @JvmStatic
    val entryWidget: EntryWidget
        get() = EntryWidgetImpl(
            activityLauncher,
            gliaThemeManager,
            controllerFactory.entryWidgetHideController,
            useCaseFactory.hasOngoingSecureConversationUseCase
        )

    @JvmStatic
    val pushNotifications: PushNotifications by lazy {
        PushNotificationsImpl(
            gliaCore.pushNotifications,
            controllerFactory.secureMessagingPushController
        )
    }

    @JvmStatic
    lateinit var repositoryFactory: RepositoryFactory
        @VisibleForTesting set

    private val uiComponentsDispatcher: UiComponentsDispatcher by lazy { UiComponentsDispatcherImpl() }

    private val authenticationRequestCallback: () -> Unit
        get() = useCaseFactory.getRequestPushNotificationDuringAuthenticationUseCase(uiComponentsDispatcher)::invoke

    @JvmStatic
    val secureConversations: SecureConversations by lazy {
        SecureConversationsImpl(gliaCore.secureConversations)
    }

    @JvmStatic
    val liveObservation: LiveObservation by lazy {
        LiveObservationImpl(gliaCore.liveObservation)
    }

    private val applicationLifecycleManager: ApplicationLifecycleManager by lazy {
        ApplicationLifecycleManager()
    }

    @Synchronized
    @JvmStatic
    fun onAppCreate(application: Application) {
        if (this::resourceProvider.isInitialized) {
            return
        }

        resourceProvider = ResourceProvider(application.baseContext)
        localeProvider = LocaleProvider(resourceProvider)
        val downloadsFolderDataSource = DownloadsFolderDataSource(application)
        val deviceMonitor = DeviceMonitor(application)
        repositoryFactory = RepositoryFactory(gliaCore, downloadsFolderDataSource, configurationManager, deviceMonitor)

        val permissionManager = PermissionManager(
            application,
            ContextCompat::checkSelfPermission,
            repositoryFactory.permissionsRequestRepository,
            Build.VERSION.SDK_INT
        )

        notificationManager = NotificationManager(application)
        val audioControlManager = AudioControlManager(application)
        useCaseFactory = UseCaseFactory(
            repositoryFactory,
            permissionManager,
            PermissionDialogManager(application),
            notificationManager,
            configurationManager,
            ChatHeadManager(application),
            audioControlManager,
            authenticationManagerProvider,
            schedulers,
            localeProvider,
            gliaCore,
            application
        )
        initAudioControlManager(audioControlManager, useCaseFactory.createOnAudioStartedUseCase())

        managerFactory = ManagerFactory(useCaseFactory)

        controllerFactory = ControllerFactory(
            repositoryFactory,
            useCaseFactory,
            managerFactory,
            gliaCore,
            applicationLifecycleManager,
            notificationManager,
            configurationManager,
            uiComponentsDispatcher
        )
        initApplicationLifecycleObserver(applicationLifecycleManager, controllerFactory.chatHeadController)

        val callVisualizerActivityWatcher = CallVisualizerActivityWatcher(
            controllerFactory.callVisualizerController,
            GliaActivityManagerImpl(),
            localeProvider,
            gliaThemeManager,
            activityLauncher
        )

        application.registerActivityLifecycleCallbacks(callVisualizerActivityWatcher)

        val activityWatcherForChatHead = ActivityWatcherForChatHead(
            controllerFactory.activityWatcherForChatHeadController,
            activityLauncher
        )
        application.registerActivityLifecycleCallbacks(activityWatcherForChatHead)

        val activityWatcherForLiveObservation = ActivityWatcherForLiveObservation(
            localeProvider,
            gliaThemeManager,
            controllerFactory.activityWatcherForLiveObservationController
        )
        application.registerActivityLifecycleCallbacks(activityWatcherForLiveObservation)

        val activityWatcherForPermissionsRequest = ActivityWatcherForPermissionsRequest(
            controllerFactory.permissionsController
        )
        application.registerActivityLifecycleCallbacks(activityWatcherForPermissionsRequest)

        callVisualizerManager = CallVisualizerManager(
            useCaseFactory.visitorCodeViewBuilderUseCase,
            controllerFactory.callVisualizerController
        )

        val engagementCompletionActivityWatcher = EngagementCompletionActivityWatcher(
            controllerFactory.endEngagementController,
            GliaActivityManagerImpl(),
            activityLauncher
        )
        application.registerActivityLifecycleCallbacks(engagementCompletionActivityWatcher)

        val operatorRequestActivityWatcher = OperatorRequestActivityWatcher(
            controllerFactory.operatorRequestController,
            activityLauncher,
            GliaActivityManagerImpl()
        )
        application.registerActivityLifecycleCallbacks(operatorRequestActivityWatcher)

        application.registerActivityLifecycleCallbacks(
            UiComponentsActivityWatcher(
                GliaActivityManagerImpl(),
                uiComponentsDispatcher,
                localeProvider,
                gliaThemeManager,
                activityLauncher
            )
        )
    }

    @JvmStatic
    fun onSdkInit(gliaWidgetsConfig: GliaWidgetsConfig) {
        val gliaConfig = createGliaConfig(gliaWidgetsConfig)
        initLogger(gliaConfig)
        gliaCore.init(gliaConfig)
        controllerFactory.init()
        repositoryFactory.initialize()
        configurationManager.applyConfiguration(gliaWidgetsConfig)
        localeProvider.setCompanyName(gliaWidgetsConfig.companyName)
        GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
    }

    @JvmStatic
    fun onSdkInit(gliaWidgetsConfig: GliaWidgetsConfig, callback: RequestCallback<Boolean?>? = null) {
        val gliaConfig = createGliaConfig(gliaWidgetsConfig)
        initLogger(gliaConfig)
        gliaCore.init(gliaConfig) { success, error ->
            if (error == null) {
                controllerFactory.init()
                repositoryFactory.initialize()
                configurationManager.applyConfiguration(gliaWidgetsConfig)
                localeProvider.setCompanyName(gliaWidgetsConfig.companyName)
            }
            callback?.onResult(success, error)
            GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURED)
        }
    }

    private fun initLogger(
        gliaConfig: GliaConfig
    ) {
        TelemetryHelper.init(gliaConfig)
        GliaTelemetry.setGlobalAttribute(GlobalAttribute.SdkWidgetsVersion, BuildConfig.GLIA_WIDGETS_SDK_VERSION)
        GliaLogger.i(LogEvents.WIDGETS_SDK_CONFIGURING) {
            put(EventAttribute.ApiKeyId, gliaConfig.siteApiKey?.id ?: "N/A")
            put(EventAttribute.Environment, gliaConfig.region ?: "N/A")
            put(EventAttribute.LocaleCode, gliaConfig.manualLocaleOverride ?: "N/A")
        }
    }

    private fun createGliaConfig(gliaWidgetsConfig: GliaWidgetsConfig): GliaConfig {
        val builder = GliaConfig.Builder()
        gliaWidgetsConfig.siteApiKey?.let {
            builder.setSiteApiKey(it.toCoreType())
        } ?: throw RuntimeException("Site key or app token is missing")
        return builder
            .setSiteId(gliaWidgetsConfig.siteId)
            .setRegion(gliaWidgetsConfig.region)
            .setBaseDomain(gliaWidgetsConfig.baseDomain)
            .setContext(gliaWidgetsConfig.context)
            .setManualLocaleOverride(gliaWidgetsConfig.manualLocaleOverride)
            .build()
    }

    @JvmStatic
    fun gliaCore(): GliaCore {
        return gliaCore
    }

    @JvmStatic
    fun getAuthenticationManager(behavior: Authentication.Behavior): AuthenticationManager =
        AuthenticationManager(gliaCore.getAuthentication(behavior.toCoreType()), authenticationRequestCallback).apply {
            authenticationManagerProvider.authenticationManager = this
        }

    private fun initApplicationLifecycleObserver(
        lifecycleManager: ApplicationLifecycleManager,
        chatBubbleController: ChatHeadContract.Controller
    ) {
        lifecycleManager.addObserver { _, event: Lifecycle.Event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Moved to on pause due to "IllegalStateException: Not allowed to start service app is in background"
                    // Related bug ticket MOB-4011
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && GliaWidgets.isInitialized()) {
                        notificationManager.startNotificationRemovalService()
                    }
                }

                Lifecycle.Event.ON_STOP -> chatBubbleController.onApplicationStop()
                Lifecycle.Event.ON_DESTROY -> chatBubbleController.onDestroy()
                else -> { /* no-op */
                }
            }
        }
    }

    private fun initAudioControlManager(
        audioControlManager: AudioControlManager,
        onAudioStartedUseCase: OnAudioStartedUseCase
    ) {
        audioControlManager.init(onAudioStartedUseCase)
    }

    fun destroyControllers() {
        controllerFactory.destroyControllers()
    }

    @JvmStatic
    fun destroyControllersAndResetEngagementData() {
        repositoryFactory.engagementRepository.reset()

        destroyControllers()
        //This function is called when the clear visitor session or de-authenticate is called
        //so these are the cases, where potentially the dialog can be shown
        uiComponentsDispatcher.dismissDialog()
    }

    fun destroyControllersAndResetQueueing() {
        controllerFactory.destroyControllersForAuthentication()
        repositoryFactory.engagementRepository.cancelQueuing()
    }

    internal class AuthenticationManagerProvider {
        @JvmField
        var authenticationManager: Authentication? = null
    }
}
