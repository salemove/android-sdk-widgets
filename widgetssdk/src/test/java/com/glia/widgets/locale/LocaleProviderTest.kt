package com.glia.widgets.locale

import com.glia.widgets.R
import com.glia.widgets.helper.IResourceProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the key-pair substitution path on [LocaleProvider.getString].
 *
 * Until the legacy `StringProvider.getRemoteString(Int, vararg com.glia.widgets.StringKeyPair)`
 * backport was removed, this formatting path had a second entry point. `getString` is now the
 * only way in, so it carries the coverage on its own.
 */
@RunWith(RobolectricTestRunner::class)
class LocaleProviderTest {

    private lateinit var resourceProvider: IResourceProvider
    private lateinit var localeProvider: LocaleProvider

    @Before
    fun setUp() {
        resourceProvider = mockk(relaxed = true)
        localeProvider = object : LocaleProvider(resourceProvider) {
            override fun getStringInternal(stringKey: Int, values: List<StringKeyPair>): String =
                remoteStrings[stringKey]
                    ?.let { template -> values.fold(template) { acc, pair -> acc.replace("{${pair.key.value}}", valueOrCompanyName(pair)) } }
                    ?: ""
        }
    }

    private val remoteStrings = mutableMapOf<Int, String>()

    @Test
    fun `getString substitutes a single key pair into the template`() {
        remoteStrings[R.string.android_chat_file_accessibility] = "File {name}"

        val result = localeProvider.getString(
            R.string.android_chat_file_accessibility,
            StringKeyPair(StringKey.NAME, "report.pdf")
        )

        assertEquals("File report.pdf", result)
    }

    @Test
    fun `getString substitutes every key pair when several are supplied`() {
        remoteStrings[R.string.android_chat_file_accessibility] = "File {name}, {size}, {status}"

        val result = localeProvider.getString(
            R.string.android_chat_file_accessibility,
            StringKeyPair(StringKey.NAME, "report.pdf"),
            StringKeyPair(StringKey.SIZE, "2 MB"),
            StringKeyPair(StringKey.STATUS, "uploaded")
        )

        assertEquals("File report.pdf, 2 MB, uploaded", result)
    }

    @Test
    fun `getString substitutes the company name for the COMPANY_NAME key`() {
        remoteStrings[R.string.general_company_name] = ""
        remoteStrings[R.string.android_chat_file_accessibility] = "Chat with {companyName}"
        localeProvider.setCompanyName("Glia")

        val result = localeProvider.getString(
            R.string.android_chat_file_accessibility,
            StringKeyPair(StringKey.COMPANY_NAME, "ignored")
        )

        assertEquals("Chat with Glia", result)
    }

    @Test
    fun `getString reads the company name instead of substituting for the company name key itself`() {
        remoteStrings[R.string.general_company_name] = ""
        localeProvider.setCompanyName("Glia")

        assertEquals("Glia", localeProvider.getString(R.string.general_company_name))
    }

    @Test
    fun `getString accepts a LocaleString and applies its values`() {
        remoteStrings[R.string.android_chat_file_accessibility] = "File {name}"

        val result = localeProvider.getString(
            LocaleString(
                R.string.android_chat_file_accessibility,
                StringKeyPair(StringKey.NAME, "report.pdf")
            )
        )

        assertEquals("File report.pdf", result)
    }

    @Test
    fun `getString leaves the template untouched when no values are supplied`() {
        remoteStrings[R.string.general_unknown] = "Unknown"

        assertEquals("Unknown", localeProvider.getString(R.string.general_unknown))
    }

    @Test
    fun `setCompanyName falls back to an empty string when no company name is set`() {
        remoteStrings[R.string.general_company_name] = ""

        every { resourceProvider.getString(R.string.general_company_name) } returns ""

        assertEquals("", localeProvider.getString(R.string.general_company_name))
    }
}
