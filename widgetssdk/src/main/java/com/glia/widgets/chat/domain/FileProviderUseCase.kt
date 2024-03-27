package com.glia.widgets.chat.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.glia.widgets.helper.fileProviderAuthority
import java.io.File

internal interface FileProviderUseCase {
    fun getUriForFile(file: File): Uri
}

internal class FileProviderUseCaseImpl(private val context: Context) : FileProviderUseCase {
    private val authority: String by lazy { context.fileProviderAuthority }
    override fun getUriForFile(file: File): Uri = FileProvider.getUriForFile(context, authority, file)
}
