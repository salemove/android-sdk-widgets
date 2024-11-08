package android

import android.content.Intent
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.subscribers.TestSubscriber
import java.nio.charset.StandardCharsets

internal const val COMMON_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.CommonExtensionsKt"
internal const val CONTEXT_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.ContextExtensions"
internal const val FILE_HELPER_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.FileHelper"
internal const val LOGGER_PATH = "com.glia.widgets.helper.Logger"

fun <T> Class<T>.readRawResource(resName: String): String = classLoader?.getResourceAsStream(resName)?.run {
    bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
} ?: ""

val Intent.targetActivityName: String? get() = component?.shortClassName

fun <T : Any> TestSubscriber<T>.assertCurrentValue(expected: T): TestSubscriber<T> = run {
    assertValueAt(values().lastIndex, expected)
}

fun <T : Any> TestSubscriber<T>.assertCurrentValue(predicate: Predicate<T>): TestSubscriber<T> = run {
    assertValueAt(values().lastIndex, predicate)
}
