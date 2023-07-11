package com.glia.widgets.core.locales

import android.content.Context
import io.reactivex.Single
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

class ReadCachedLocaleUseCase {
    fun execute(context: Context, locale: String): Single<NewLocale> {
        return Single.create {
            val path = "${context.cacheDir}/$locale.locale"
            val fis = FileInputStream(File(path))
            val ois = ObjectInputStream(fis)
            val newLocale = ois.readObject() as NewLocale
            ois.close()
            fis.close()
            it.onSuccess(newLocale)
        }
    }
}
