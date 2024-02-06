package android

import android.content.Intent
import java.nio.charset.StandardCharsets

internal const val COMMON_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.CommonExtensionsKt"
internal const val LOGGER_PATH = "com.glia.widgets.helper.Logger"

fun <T> Class<T>.readRawResource(resName: String): String = classLoader?.getResourceAsStream(resName)?.run {
    bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
} ?: ""

val Intent.targetActivityName: String? get() = component?.shortClassName
