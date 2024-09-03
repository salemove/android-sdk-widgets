package com.glia.widgets.di

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaConfig
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.Navigator
import com.glia.widgets.StringProvider
import com.glia.widgets.callvisualizer.CallVisualizerActivityWatcher
import com.glia.widgets.core.audio.AudioControlManager
import com.glia.widgets.core.audio.domain.OnAudioStartedUseCase
import com.glia.widgets.core.authentication.AuthenticationManager
import com.glia.widgets.core.callvisualizer.CallVisualizerManager
import com.glia.widgets.core.chathead.ChatHeadManager
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.PermissionDialogManager
import com.glia.widgets.core.notification.device.INotificationManager
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.completion.EngagementCompletionActivityWatcher
import com.glia.widgets.filepreview.data.source.local.DownloadsFolderDataSource
import com.glia.widgets.helper.ApplicationLifecycleManager
import com.glia.widgets.helper.GliaActivityManagerImpl
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.IntentConfigurationHelperImpl
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.operator.OperatorRequestActivityWatcher
import com.glia.widgets.permissions.ActivityWatcherForPermissionsRequest
import com.glia.widgets.view.head.ActivityWatcherForChatHead
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservation
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
    var sdkConfigurationManager: GliaSdkConfigurationManager = GliaSdkConfigurationManager()
        @VisibleForTesting set
    val intentConfigurationHelper: IntentConfigurationHelper = IntentConfigurationHelperImpl()
    @JvmStatic
    lateinit var repositoryFactory: RepositoryFactory
        @VisibleForTesting set

    @JvmStatic
    fun onAppCreate(application: Application) {
        resourceProvider = ResourceProvider(application.baseContext)
        localeProvider = LocaleProvider(resourceProvider)
        notificationManager = NotificationManager(application)
        val downloadsFolderDataSource = DownloadsFolderDataSource(application)
        repositoryFactory = RepositoryFactory(gliaCore, downloadsFolderDataSource)

        val permissionManager = PermissionManager(
            application,
            ContextCompat::checkSelfPermission,
            repositoryFactory.permissionsRequestRepository,
            Build.VERSION.SDK_INT
        )
        val audioControlManager = AudioControlManager(application)
        useCaseFactory = UseCaseFactory(
            repositoryFactory,
            permissionManager,
            PermissionDialogManager(application),
            notificationManager,
            sdkConfigurationManager,
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
            sdkConfigurationManager,
            managerFactory
        )
        initApplicationLifecycleObserver(
            ApplicationLifecycleManager(),
            controllerFactory.chatHeadController
        )

        val callVisualizerActivityWatcher = CallVisualizerActivityWatcher(
            controllerFactory.callVisualizerController,
            GliaActivityManagerImpl(),
            localeProvider,
            gliaThemeManager
        )

        application.registerActivityLifecycleCallbacks(callVisualizerActivityWatcher)

        val activityWatcherForChatHead = ActivityWatcherForChatHead(
                controllerFactory.activityWatcherForChatHeadController
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
            GliaActivityManagerImpl()
        )
        application.registerActivityLifecycleCallbacks(engagementCompletionActivityWatcher)

        val operatorRequestActivityWatcher = OperatorRequestActivityWatcher(
            controllerFactory.operatorRequestController,
            intentConfigurationHelper,
            GliaActivityManagerImpl()
        )
        application.registerActivityLifecycleCallbacks(operatorRequestActivityWatcher)
    }

    @JvmStatic
    fun onSdkInit(gliaWidgetsConfig: GliaWidgetsConfig) {
        val gliaConfig = createGliaConfig(gliaWidgetsConfig)
        gliaCore.init(gliaConfig)
        controllerFactory.init()
        repositoryFactory.engagementRepository.initialize()
        sdkConfigurationManager.fromConfiguration(gliaWidgetsConfig)
        localeProvider.setCompanyName(gliaWidgetsConfig.companyName)
    }

    private fun createGliaConfig(gliaWidgetsConfig: GliaWidgetsConfig): GliaConfig {
        val builder = GliaConfig.Builder()
        gliaWidgetsConfig.siteApiKey?.let {
            builder.setSiteApiKey(it)
        } ?: throw RuntimeException("Site key or app token is missing")
        return builder
            .setSiteId(gliaWidgetsConfig.siteId)
            .setRegion(gliaWidgetsConfig.region)
            .setBaseDomain(gliaWidgetsConfig.baseDomain)
            .setContext(gliaWidgetsConfig.context)
            .setManualLocaleOverride(gliaWidgetsConfig.manualLocaleOverride)
            .build()
    }

    @Deprecated(" This feature is not public anymore")
    val stringProvider: StringProvider
        get() = localeProvider

    @JvmStatic
    fun glia(): GliaCore {
        return gliaCore
    }

    @JvmStatic
    fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        authenticationManagerProvider.authenticationManager = authenticationManager
    }

    @JvmStatic
    fun getNavigator(queues: ArrayList<String>?): Navigator {
        return Navigator(queues, null)
    }

    private fun initApplicationLifecycleObserver(
        lifecycleManager: ApplicationLifecycleManager,
        chatBubbleController: ChatHeadContract.Controller
    ) {
        lifecycleManager.addObserver { source: LifecycleOwner?, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_STOP) {
                chatBubbleController.onApplicationStop()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && Glia.isInitialized()) {
                    notificationManager.startNotificationRemovalService()
                }
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                chatBubbleController.onDestroy()
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
        destroyControllers()
        repositoryFactory.engagementRepository.reset()
    }

    fun destroyControllersAndResetQueueing() {
        controllerFactory.destroyControllersForAuthentication()
        repositoryFactory.engagementRepository.resetQueueing()
    }

    internal class AuthenticationManagerProvider {
        @JvmField
        var authenticationManager: AuthenticationManager? = null
    }
}
