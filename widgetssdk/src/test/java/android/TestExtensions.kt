package android

import java.nio.charset.StandardCharsets

fun <T> Class<T>.readRawResource(resName: String): String = classLoader?.getResourceAsStream(resName)?.run {
    bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
} ?: ""
