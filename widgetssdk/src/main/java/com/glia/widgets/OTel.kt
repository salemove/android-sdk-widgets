package com.glia.widgets

import android.app.Activity
import com.glia.androidsdk.internal.logger.SessionDebugData
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.messagecenter.MessageCenterActivity
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.Scope
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object OTel {

    val instance: OpenTelemetrySdk

    private val defaultTracerScope: Tracer
    private var appLifeSpan: Span
    private var sessionScope: Scope
    private val activitySpamMap = HashMap<ActivityType, Span>()
    private var topActivityRef: WeakReference<Activity>? = null

    init {
        val jaegerOtlpExporter: OtlpHttpSpanExporter =
            OtlpHttpSpanExporter.builder()
//                .addHeader("api-key", "value")
                .setEndpoint("https://opentelemetry.aleksandr-chatsky.dev.samo.io/v1/traces")
                .setTimeout(3000, TimeUnit.SECONDS)
                .build()

        val jaegerBatchSpanProcessor = BatchSpanProcessor.builder(jaegerOtlpExporter)
            .setMaxExportBatchSize(500)
            .setScheduleDelay(5, TimeUnit.SECONDS)
            .setExporterTimeout(3000, TimeUnit.SECONDS)
            .build()

        val resource = Resource.getDefault().toBuilder()
            .put("service.name", "mobile_sdk")
            .build()

        val tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(jaegerBatchSpanProcessor)
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .setResource(resource)
                .build()

        instance = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()

        defaultTracerScope = instance.getTracer("android_sdk_tracer")

        appLifeSpan = defaultTracerScope
            .spanBuilder("App: Session")
            .setAttribute("sdk_widgets_version", GliaWidgets.widgetsSdkVersion)
            .setAttribute("sdk_core_version", GliaWidgets.widgetsCoreSdkVersion)
            .startSpan()

        sessionScope = appLifeSpan.makeCurrent()

        Runtime.getRuntime().addShutdownHook(Thread{
            appLifeSpan.end()
            sessionScope.close()
            tracerProvider.shutdown()
        })

    }

    fun Span.addSessionInfo(context: android.content.Context): Span {
        val sessionInfo = SessionDebugData(context)
        setAttribute("app_version", sessionInfo.client.appVersion)
        setAttribute("app_name", context.applicationInfo.loadLabel(context.packageManager).toString())
        setAttribute("app_bundle_id", sessionInfo.client.packageName)
        setAttribute("device_name", sessionInfo.device.getFullName())
        setAttribute("device_os_version", sessionInfo.device.osVersion)
        setAttribute("device_os_type", "Android")
        return this
    }

    fun newSpan(spanName: String, customAttributes: Attributes? = null): SpanBuilder {
        return defaultTracerScope
            .spanBuilder(spanName)
            .setParent(Context.current().with(appLifeSpan))
            .apply { customAttributes?.let { setAllAttributes(it) } }
    }

    fun trace(spanName: String, customAttributes: Attributes? = null, codeBlock: (Span) -> Unit) {
        val span = newSpan(spanName, customAttributes).startSpan()
        try {
            codeBlock(span)
        }catch (error: Throwable) {
            span.setStatus(StatusCode.ERROR)
            span.recordException(error)
        } finally {
            span.end()
        }
    }

    fun closeSessionSpan() {
        appLifeSpan.end()
    }

    fun onActivityResumed(activity: Activity) {
        topActivityRef = WeakReference(activity)
        ActivityType.from(activity).let {
            if (it.isGlia) {
                activitySpamMap[it]?.addEvent("Activity Resumed")
                activitySpamMap[ActivityType.NOT_GLIA]?.end()
                activitySpamMap.remove(ActivityType.NOT_GLIA)
            } else {
                if (!activitySpamMap.contains(ActivityType.NOT_GLIA)) {
                    activitySpamMap[ActivityType.NOT_GLIA] = newSpan(ActivityType.NOT_GLIA.traceName).startSpan()
                }
                activitySpamMap[ActivityType.NOT_GLIA]?.addEvent("One of client's activities resumed")
            }
        }
    }

    fun onActivityStarted(activity: Activity) {
        ActivityType.from(activity).let {
            if (it.isGlia || !activitySpamMap.containsKey(it)) {
                activitySpamMap[it] = newSpan(it.traceName).startSpan()
            } else {
                activitySpamMap[ActivityType.NOT_GLIA]?.addEvent("Another client's activity started")
            }
        }
    }

    fun onActivityPaused(activity: Activity) {
        ActivityType.from(activity).let {
            if (it.isGlia) {
                activitySpamMap[it]?.addEvent("Activity Paused")
            } else {
                activitySpamMap[ActivityType.NOT_GLIA]?.addEvent("One of client's activities paused")
            }
        }
    }

    fun onActivityDestroyed(activity: Activity) {
        ActivityType.from(activity).let {
            if (it.isGlia) {
                activitySpamMap[it]?.end()
                activitySpamMap.remove(it)
            } else {
                activitySpamMap[ActivityType.NOT_GLIA]?.addEvent("One of client's activities destroyed")
            }
        }
    }

    fun onAppPaused() {
        topActivityRef?.get()?.let { activity ->
            ActivityType.from(activity).let { activityType ->
                activitySpamMap[activityType]?.addEvent("App went into background")
            }
        }
    }

    fun onAppResumed() {
        topActivityRef?.get()?.let { activity ->
            ActivityType.from(activity).let { activityType ->
                activitySpamMap[activityType]?.addEvent("App returned into foreground")
            }
        }
    }

    fun getActivitySpan(activityType: ActivityType): Span? {
        return activitySpamMap[activityType]
    }
}

public fun attributes(key: String, value: String?): Attributes {
    return Attributes.builder().put(key, value ?: "").build()
}

enum class ActivityType(val traceName: String, val isGlia: Boolean) {
    CHAT_SCREEN("Screen: Glia Chat", true),
    CALL_SCREEN("Screen: Glia Call", true),
    SC_WELCOME_SCREEN("Screen: Glia SC Welcome", true),
    OTHER_GLIA("Screen: Glia Unknown", true),
    NOT_GLIA("Screen: Client's App", false);

    companion object {
        fun from(activity: Activity): ActivityType {
            if (!activity.isGliaActivity) {
                return NOT_GLIA
            }
            return when (activity) {
                is ChatActivity -> CHAT_SCREEN
                is CallActivity -> CALL_SCREEN
                is MessageCenterActivity -> SC_WELCOME_SCREEN
                else -> OTHER_GLIA
            }
        }
    }
}

private val Activity.isGliaActivity: Boolean
    get() {
        return localClassName.startsWith("com.glia.widgets.")
    }
