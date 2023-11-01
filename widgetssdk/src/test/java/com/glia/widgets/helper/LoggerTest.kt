package com.glia.widgets.helper

import com.glia.androidsdk.LogLevel
import com.glia.androidsdk.LoggingAdapter
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import java.util.Collections

class LoggerTest {
    @Test
    fun `loggingAdapter logs messages`() {
        val error: Throwable = RuntimeException()
        val loggingAdapter = Mockito.mock(LoggingAdapter::class.java)
        Logger.addAdapter(loggingAdapter)
        Logger.d(LoggerTest.TAG, MESSAGE)
        Logger.i(LoggerTest.TAG, MESSAGE)
        Logger.w(LoggerTest.TAG, MESSAGE)
        Logger.e(LoggerTest.TAG, MESSAGE, error)
        Mockito.verify(loggingAdapter).log(LogLevel.DEBUG, LoggerTest.TAG, MESSAGE, emptyMap())
        Mockito.verify(loggingAdapter).log(LogLevel.INFO, LoggerTest.TAG, MESSAGE, emptyMap())
        Mockito.verify(loggingAdapter).log(LogLevel.WARN, LoggerTest.TAG, MESSAGE, emptyMap())
        Mockito.verify(loggingAdapter).error(LoggerTest.TAG, MESSAGE, error, emptyMap())
    }

    @Test
    fun `loggingAdapter logs messages with metadata`() {
        val error: Throwable = RuntimeException()
        val loggingAdapter = Mockito.mock(LoggingAdapter::class.java)
        Logger.addAdapter(loggingAdapter)
        val metadata = Collections.singletonMap("key", "value")
        Logger.d(LoggerTest.TAG, MESSAGE, metadata)
        Logger.i(LoggerTest.TAG, MESSAGE, metadata)
        Logger.w(LoggerTest.TAG, MESSAGE, metadata)
        Logger.e(LoggerTest.TAG, MESSAGE, error, metadata)
        Mockito.verify(loggingAdapter).log(LogLevel.DEBUG, LoggerTest.TAG, MESSAGE, metadata)
        Mockito.verify(loggingAdapter).log(LogLevel.INFO, LoggerTest.TAG, MESSAGE, metadata)
        Mockito.verify(loggingAdapter).log(LogLevel.WARN, LoggerTest.TAG, MESSAGE, metadata)
        Mockito.verify(loggingAdapter).error(LoggerTest.TAG, MESSAGE, error, metadata)
    }

    @Test
    fun `loggingAdapter logs messages with global metadata`() {
        val error: Throwable = RuntimeException()
        val loggingAdapter = Mockito.mock(LoggingAdapter::class.java)
        Logger.addAdapter(loggingAdapter)
        val metadata = Collections.singletonMap("key", "value")
        val globalMeta = Collections.singletonMap("globalKey", "value0")
        val combinedMetadata: MutableMap<String, String> = HashMap()
        combinedMetadata.putAll(metadata)
        combinedMetadata.putAll(globalMeta)
        Logger.addGlobalMetadata(globalMeta)
        Logger.d(LoggerTest.TAG, MESSAGE, metadata)
        Logger.i(LoggerTest.TAG, MESSAGE, metadata)
        Logger.w(LoggerTest.TAG, MESSAGE, metadata)
        Logger.e(LoggerTest.TAG, MESSAGE, error, metadata)
        Mockito.verify(loggingAdapter).log(LogLevel.DEBUG, LoggerTest.TAG, MESSAGE, combinedMetadata)
        Mockito.verify(loggingAdapter).log(LogLevel.INFO, LoggerTest.TAG, MESSAGE, combinedMetadata)
        Mockito.verify(loggingAdapter).log(LogLevel.WARN, LoggerTest.TAG, MESSAGE, combinedMetadata)
        Mockito.verify(loggingAdapter).error(LoggerTest.TAG, MESSAGE, error, combinedMetadata)
        Logger.removeGlobalMetadata("globalKey")
    }

    @Test
    fun `logger logs only locally when loggingAdapters list empty`() {
        val error: Throwable = RuntimeException()
        val loggingAdapter = Mockito.mock(LoggingAdapter::class.java)
        val metadata = Collections.singletonMap("key", "value")
        val globalMeta = Collections.singletonMap("globalKey", "value0")
        val combinedMetadata: MutableMap<String, String> = HashMap()
        combinedMetadata.putAll(metadata)
        combinedMetadata.putAll(globalMeta)
        Logger.addGlobalMetadata(globalMeta)
        Logger.d(LoggerTest.TAG, MESSAGE, metadata)
        Logger.i(LoggerTest.TAG, MESSAGE, metadata)
        Logger.w(LoggerTest.TAG, MESSAGE, metadata)
        Logger.e(LoggerTest.TAG, MESSAGE, error, metadata)
        Mockito.verify(loggingAdapter, never()).log(any(), any(), any(), any())
        Logger.removeGlobalMetadata("globalKey")
    }

    companion object {
        private const val TAG = "tag"
        private const val MESSAGE = "message"
    }
}
