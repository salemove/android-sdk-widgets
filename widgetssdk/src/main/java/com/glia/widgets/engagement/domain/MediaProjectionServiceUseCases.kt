package com.glia.widgets.engagement.domain

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.glia.widgets.core.screensharing.MediaProjectionService

internal interface StartMediaProjectionServiceUseCase {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke()
}

internal class StartMediaProjectionServiceUseCaseImpl(private val context: Context) : StartMediaProjectionServiceUseCase {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun invoke() {
        context.startForegroundService(Intent(context, MediaProjectionService::class.java))
    }
}

internal interface StopMediaProjectionServiceUseCase {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke()
}

internal class StopMediaProjectionServiceUseCaseImpl(private val context: Context) : StopMediaProjectionServiceUseCase {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun invoke() {
        context.stopService(Intent(context, MediaProjectionService::class.java))
    }
}
