package android

import android.content.Intent
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.GliaTelemetry
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.subscribers.TestSubscriber
import java.nio.charset.StandardCharsets

// This file is meant to write extensions that are used in the tests.

// Common extension functions paths for mocking
internal const val COMMON_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.CommonExtensionsKt"
internal const val CONTEXT_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.ContextExtensions"
internal const val FILE_HELPER_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.FileHelper"
internal const val LOGGER_PATH = "com.glia.widgets.helper.Logger"
internal const val DEPS_PATH = "com.glia.widgets.di.Dependencies"
internal const val GLIA_TELEMETRY_PATH = "com.glia.telemetry_lib.GliaTelemetry"
internal const val GLIA_LOGGER_PATH = "com.glia.telemetry_lib.GliaLogger"

fun <T> Class<T>.readRawResource(resName: String): String = classLoader?.getResourceAsStream(resName)?.run {
    bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
} ?: ""

val Intent.targetActivityName: String? get() = component?.shortClassName

/**
 * Since there is no way out of the box to assert the current value of a TestSubscriber, this function does it using [TestSubscriber.assertValueAt]
 * function with the latest index of the values list.
 */
fun <T : Any> TestSubscriber<T>.assertCurrentValue(expected: T): TestSubscriber<T> = run {
    assertValueAt(values().lastIndex, expected)
}

/**
 * Since there is no way out of the box to assert the current value of a TestSubscriber, this function does it using [TestSubscriber.assertValueAt]
 * function with the latest index of the values list.
 */
fun <T : Any> TestSubscriber<T>.assertCurrentValue(predicate: Predicate<T>): TestSubscriber<T> = run {
    assertValueAt(values().lastIndex, predicate)
}

//Extension function to mock the static logger
internal fun Logger.mock() {
    mockkStatic(LOGGER_PATH)
    setIsDebug(false)
    every { d(any(), any()) } just Runs
    every { e(any(), any()) } just Runs
    every { w(any(), any()) } just Runs
    every { i(any(), any()) } just Runs
}

//Extension function to unMock the static logger
internal fun Logger.unMock() {
    unmockkStatic(LOGGER_PATH)
}

// Extension function to mock the [Dependencies] class
internal fun Dependencies.mock() {
    mockkStatic(DEPS_PATH)
}

// Extension function to unMock the [Dependencies] class
internal fun Dependencies.unMock() {
    unmockkStatic(DEPS_PATH)
}

//Extension function to mock the [OneTimeEvent] class, that is used in multiple places.
internal fun <T : Any> mockkOneTimeEvent(value: T, isConsumed: Boolean = false): OneTimeEvent<T> =
    mockk(relaxUnitFun = true) {
        every { consumed } returns isConsumed
        every { this@mockk.value } returns value
        every { consume(captureLambda()) } answers {
            firstArg<T.() -> Unit>().invoke(value)
        }
    }

// Extension functions to mock and unMock GliaTelemetry
internal fun GliaTelemetry.mockk() {
    mockkStatic(GLIA_TELEMETRY_PATH)
}

internal fun GliaTelemetry.unMockk() {
    unmockkStatic(GLIA_TELEMETRY_PATH)
}

// Extension functions to mock and unMock GliaLogger
internal fun GliaLogger.mockk() {
    mockkStatic(GLIA_LOGGER_PATH)
}

internal fun GliaLogger.unMockk() {
    unmockkStatic(GLIA_LOGGER_PATH)
}
