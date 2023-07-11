package com.glia.widgets.core.locales

import android.content.Context
import io.reactivex.Completable
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream

internal class CacheLocaleUseCase(private val cachedFileNamesUseCase: CachedFileNamesUseCase) {
    fun execute(context: Context, newLocale: NewLocale, locale: String): Completable {
        return Completable.fromAction {
            val file = File("${context.cacheDir}/$locale.locale")
            val fos = FileOutputStream(file)
            val os = ObjectOutputStream(fos)
            os.writeObject(newLocale)
            os.close()
            fos.close()
        }.andThen(cachedFileNamesUseCase.store(locale))
    }
}
