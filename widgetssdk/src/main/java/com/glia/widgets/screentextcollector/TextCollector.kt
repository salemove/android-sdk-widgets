package com.glia.widgets.screentextcollector

import android.app.Application
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import java.lang.ref.WeakReference
import java.security.MessageDigest
import androidx.core.view.isVisible
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.entrywidget.EntryWidgetActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class TextCollector(
    private val engagementStateUseCase: EngagementStateUseCase,
) {
    private var application: WeakReference<Application>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var settings = TextCollectorSettings()
    private val history = ScreenTextHistory(settings.maxHistorySize)
    private var onScreenTextCollectedListener: ((ScreenTextData) -> Unit)? = null
    private var textCollectorHelper = TextCollectorHelper()
    private var textCollectorDialogDetector = TextCollectorDialogDetector(textCollectorHelper)
    private val singleActivityContentObservers = mutableMapOf<Activity, SingleActivityContentChangeObserver>()
    private var lifecycleObserver: Application.ActivityLifecycleCallbacks? = null
    private val engagementStateObservable by lazy { engagementStateUseCase() }

    fun initialize(
        application: Application,
        maxScreensToKeep: Int = settings.maxHistorySize,
    ) {
        Logger.d(TAG, "TextCollector -> initialize")
        this.application = WeakReference(application)
        this.settings = settings.copy(maxHistorySize = maxScreensToKeep)
        history.setMaxScreenNumber(maxScreensToKeep)
        start()

        engagementStateObservable.subscribe { state ->
            when (state) {
                is State.NoEngagement, is State.EngagementEnded, is State.QueueUnstaffed,
                is State.QueueingCanceled, is State.UnexpectedErrorHappened -> start()

                is State.Queuing, is State.EngagementStarted -> stop()

                is State.PreQueuing, is State.Update, is State.TransferredToSecureConversation -> { /* Ignore */
                }
            }
        }
    }

    private fun start() {
        if (lifecycleObserver == null) {
            Logger.d(TAG, "TextCollector -> Start")
            lifecycleObserver = prepareActivityLifecycleCallbacks()
            application?.get()?.registerActivityLifecycleCallbacks(lifecycleObserver)
        }
    }

    private fun stop() {
        if (lifecycleObserver != null) {
            Logger.d(TAG, "TextCollector -> Stop")
            handler.removeCallbacksAndMessages(null)
            application?.get()?.unregisterActivityLifecycleCallbacks(lifecycleObserver)
            lifecycleObserver = null
        }
    }

    private fun shutdown() {
        if (lifecycleObserver != null) {
            handler.removeCallbacksAndMessages(null)
            singleActivityContentObservers.values.forEach { it.stop() }
            singleActivityContentObservers.clear()
            application?.get()?.unregisterActivityLifecycleCallbacks(lifecycleObserver)
            lifecycleObserver = null
            application = null
            history.clear()
            onScreenTextCollectedListener = null
        }
    }

    fun getScreenHistory(): List<ScreenTextData> = history.getAll()

    fun setOnScreenTextCollectedListener(listener: (ScreenTextData) -> Unit) {
        this.onScreenTextCollectedListener = listener
    }

    private fun prepareActivityLifecycleCallbacks(): Application.ActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            if (activity is GliaActivity<*> || activity is EntryWidgetActivity) return // Collect texts only from non-Glia screens
            if (!singleActivityContentObservers.containsKey(activity)) {
                val observer = SingleActivityContentChangeObserver(activity)
                singleActivityContentObservers[activity] = observer
                observer.start()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            singleActivityContentObservers[activity]?.stop()
            singleActivityContentObservers.remove(activity)
        }

        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            singleActivityContentObservers[activity]?.stop()
            singleActivityContentObservers.remove(activity)
        }
    }

    fun scheduleTextCollection(
        activity: Activity,
        screenName: String,
        screenType: ScreenType,
        forceCollection: Boolean = false
    ) {
        handler.postDelayed({
            val currentTime = System.currentTimeMillis()
            val texts = collectTextFromActivity(activity)
            val contentHash = calculateHash(texts)
            val hasDialog = textCollectorDialogDetector.hasDialog(
                getAllRootViews(),
            )
            val finalScreenType = if (hasDialog) ScreenType.DIALOG else screenType
            val lastEntry = history.getMostRecent()
            if (lastEntry != null && (currentTime - lastEntry.timestamp) < settings.minCollectionIntervalMs && !forceCollection) {
                // Remove last entry if too soon
                Logger.d(TAG, "Removing last entry - too short time spent on the screen")
                history.removeMostRecent()
            }
            if (lastEntry == null || lastEntry.contentHash != contentHash) {
                val data = ScreenTextData(
                    screenName = screenName,
                    timestamp = currentTime,
                    texts = texts,
                    screenType = finalScreenType,
                    contentHash = contentHash
                )
                history.add(data)
                onScreenTextCollectedListener?.invoke(data)
                Logger.d(TAG, "New texts for ${history.getAll().size} screens.")
                history.getAll().forEach { Logger.d(TAG, "${it.screenName} ${it.texts}") }
            }
        }, settings.collectionDelayMs)
    }

    private fun calculateHash(texts: List<String>): String {
        val content = texts.joinToString("|")
        val bytes = MessageDigest.getInstance("MD5").digest(content.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun collectTextFromActivity(activity: Activity): List<String> {
        val texts = mutableListOf<String>()
        try {
            val allRootViews = getAllRootViews()
            if (allRootViews.isNotEmpty()) {
                Logger.d(TAG, "Collecting texts from ${allRootViews.size} root views")
                allRootViews.forEach { rootView ->
                    if (rootView.isVisible) {
                        texts.addAll(textCollectorHelper.collectTextFromViewHierarchy(rootView))
                        texts.addAll(textCollectorHelper.collectTextFromCompose(rootView))
                    }
                }
            } else {
                Logger.d(TAG, "Fallback: Collecting texts from activity window")
                collectTextsFromActivityWindow(activity, texts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.d(TAG, "Exception during collecting texts from root views $e")
            collectTextsFromActivityWindow(activity, texts)
        }
        val distinctTexts = texts.filter { it.isNotBlank() }.distinct()
        return distinctTexts
    }

    private fun collectTextsFromActivityWindow(
        activity: Activity,
        texts: MutableList<String>
    ) {
        val rootView = activity.window?.decorView
        if (rootView != null) {
            texts.addAll(textCollectorHelper.collectTextFromViewHierarchy(rootView))
            texts.addAll(textCollectorHelper.collectTextFromCompose(rootView))
        }
    }

    /*
    * getAllRootViews() returns a list of root views. It uses reflection to access the private mViews field of
    *  WindowManagerGlobal, which holds references to the root views of all windows in the current process.
    */
    private fun getAllRootViews(): List<View> {
        val rootViews = mutableListOf<View>()
        try {
            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val getInstanceMethod = windowManagerGlobalClass.getMethod("getInstance")
            val windowManagerGlobal = getInstanceMethod.invoke(null)
            val viewsField = windowManagerGlobalClass.getDeclaredField("mViews")
            viewsField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val views = viewsField.get(windowManagerGlobal) as? ArrayList<View>
            if (views != null) {
                rootViews.addAll(views)
                Logger.d(TAG, "Found ${views.size} root views:")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rootViews
    }

    /**
     * Observer for content changes in single-activity apps
     */
    private inner class SingleActivityContentChangeObserver(
        private val activity: Activity
    ) : ViewTreeObserver.OnGlobalLayoutListener {

        private var rootView: View? = null
        private var isObserving = false

        override fun onGlobalLayout() {
            Logger.d(TAG, "SingleActivityContentChangeObserver: onGlobalLayout triggered")
            // Collect text when layout changes (content/dialog changes)
            val screenName = activity.javaClass.simpleName
            scheduleTextCollection(activity, screenName, ScreenType.CONTENT_CHANGE)
        }

        fun start() {
            if (isObserving) return

            rootView = activity.window?.decorView?.rootView
            rootView?.viewTreeObserver?.addOnGlobalLayoutListener(this)
            isObserving = true
            Logger.d(TAG, "SingleActivityContentChangeObserver started for ${activity.javaClass.simpleName}")
        }

        fun stop() {
            if (!isObserving) return

            rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            rootView = null
            isObserving = false
            Logger.d(TAG, "SingleActivityContentChangeObserver stopped")
        }
    }

    companion object {
        const val TAG = "TextCollector"
    }
}
