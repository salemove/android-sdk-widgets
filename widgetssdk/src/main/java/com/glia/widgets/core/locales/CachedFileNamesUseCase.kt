package com.glia.widgets.core.locales

import android.content.SharedPreferences
import com.glia.widgets.helper.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single

class CachedFileNamesUseCase(private val sharedPrefs: SharedPreferences, private val gson: Gson) {

    fun store(locale: String): Completable {
        return retrieve().map { map ->
            map.remove(locale)
            map[locale] = System.currentTimeMillis()
            sharedPrefs.edit().putString(KEY, gson.toJson(map)).apply()
            Logger.d("CachedFileNamesUseCase", map.toString())
        }.ignoreElement()
    }

    fun retrieve(): Single<HashMap<String, Long>> {
        return Single.create {
            val type = object : TypeToken<HashMap<String?, String?>?>() {}.type
            gson.fromJson(sharedPrefs.getString(KEY, gson.toJson(hashMapOf<String, Long>())), type)
        }
    }

    companion object {
        private val KEY = "com.glia.widgets.core.locales.KEY"
    }
}
