package com.glia.widgets.locale

import android.os.Parcelable
import androidx.annotation.OpenForTesting
import androidx.annotation.StringRes
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.locale.LocaleManager
import com.glia.widgets.BuildConfig
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.helper.IResourceProvider
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.parcelize.Parcelize

/**
 * @hide
 */
@Parcelize
data class LocaleString(
    @StringRes val stringKey: Int,
    val values: List<StringKeyPair> = emptyList()
) : Parcelable {

    constructor(@StringRes stringKey: Int, vararg values: StringKeyPair) : this(stringKey, values.toList())

    constructor(@StringRes stringKey: Int, vararg values: com.glia.widgets.StringKeyPair) : this(stringKey, values.map { StringKeyPair(it) }.toList())
}

@OpenForTesting
internal open class LocaleProvider(
    private val resourceProvider: IResourceProvider
) : StringProvider {

    private var hardcodedCompanyName: String? = null
    private val regex = "(\\{[a-zA-Z\\d]*\\})".toRegex()
    private val localeObservable: Observable<String>
    private val localeEmitter: Observer<String>
    private lateinit var localeManager: LocaleManager
    private val isSnapshotTestMode = BuildConfig.BUILD_TYPE == "snapshot"

    init {
        val localeSubject = PublishSubject.create<String>()
        localeObservable = localeSubject
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .share()
        localeEmitter = localeSubject
    }

    private fun getLocaleManager(): LocaleManager {
        if (this::localeManager.isInitialized.not()) {
            localeManager = Glia.getLocaleManager()
            localeManager.onLocaleChanged { localeCode, error ->
                if (error == null && localeCode != null) {
                    localeEmitter.onNext(localeCode)
                } else {
                    Logger.e(TAG, "Locale update observable crashed", error)
                }
            }
        }
        return localeManager
    }

    // For backport support of legacy interface
    @Deprecated("Deprecated in Java")
    override fun getRemoteString(stringKey: Int, vararg values: com.glia.widgets.StringKeyPair): String {
        Logger.logDeprecatedMethodUse(TAG, "getRemoteString(stringKey: Int, vararg values: com.glia.widgets.StringKeyPair)")
        return getString(stringKey, values.map { StringKeyPair(it) })
    }

    // For backport support of legacy interface
    @Deprecated("Deprecated in Java")
    override fun reportImproperInitialisation(exception: Exception) {
        Logger.logDeprecatedMethodUse(TAG, "reportImproperInitialisation(exception: Exception)")
        // Nothing to do.
    }

    fun getString(stringKey: Int, vararg values: StringKeyPair): String {
        return getString(stringKey, values.toList())
    }

    fun getString(stringKey: Int, values: List<StringKeyPair>): String {
        return if (stringKey == R.string.general_company_name) {
            getCompanyName()
        } else {
            getStringInternal(stringKey, values)
        }
    }

    fun getString(localeString: LocaleString): String {
        return getString(localeString.stringKey, localeString.values)
    }

    @OpenForTesting
    open fun getStringInternal(stringKey: Int, values: List<StringKeyPair> = emptyList()): String {
        val key = resourceProvider.getResourceKey(stringKey)
        if (isSnapshotTestMode) {
            return key
        }
        return try {
            getLocaleManager().getRemoteString(key)?.let {
                values.fold(it, ::replaceInsertedValues).run(::cleanupRemainingReferences)
            } ?: resourceProvider.getString(stringKey, *values.map { pair -> valueOrCompanyName(pair) }.toTypedArray())
        } catch (e: GliaException) {
            Logger.d(TAG, "Remote locale string failed to load : ${e.message}")
            resourceProvider.getString(stringKey, *values.map { pair -> valueOrCompanyName(pair) }.toTypedArray())
        }
    }

    fun getLocaleObservable(): Observable<String> {
        return localeObservable
    }

    fun setCompanyName(companyName: String?) {
        hardcodedCompanyName = companyName
    }

    fun valueOrCompanyName(stringKeyPair: StringKeyPair): String {
        return if (stringKeyPair.key == StringKey.COMPANY_NAME) {
            getCompanyName()
        } else {
            stringKeyPair.value
        }
    }

    // Priority for remote company name but not found or no locales it will fall back to hardcoded one or "" if no hardcoded one
    private fun getCompanyName(): String = getStringInternal(R.string.general_company_name)
        .takeIf { it.isNotBlank() } ?: hardcodedCompanyName.orEmpty()

    private fun replaceInsertedValues(string: String, pair: StringKeyPair): String {
        return if (string.contains(pair.key.value, false)) {
            string.replace("{${pair.key.value}}", valueOrCompanyName(pair))
        } else {
            string
        }
    }

    private fun cleanupRemainingReferences(string: String): String {
        return regex.replace(string, "")
    }
}
