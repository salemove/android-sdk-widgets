package com.glia.widgets.chat.adapter.holder

/**
 * Builds the HTML prefix the SDK injects ahead of operator-supplied custom card markup.
 *
 * Stateless: holds only pure functions and constants.
 */
internal object CustomCardHtml {
    // Card script cannot revoke a CSP meta that lands in the implied <head>. frame-src blocks every
    // iframe except about:srcdoc and about:blank; form-action blocks every form submission, including
    // POST, which shouldOverrideUrlLoading never sees.
    private const val CSP_META: String =
        "<meta http-equiv=\"Content-Security-Policy\" content=\"frame-src 'none'; form-action 'none'\">"

    // A leading BOM, whitespace and comments may precede the doctype.
    private val LEADING_DOCTYPE: Regex = Regex(
        "^\\uFEFF?(\\s|<!--.*?-->)*<!doctype[^>]*>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    /** Inserts [prefix] after a leading doctype, or at the start, so the document never enters quirks mode. */
    fun withPrefix(html: String, prefix: String): String {
        val insertAt = LEADING_DOCTYPE.find(html)?.range?.last?.plus(1) ?: 0
        return html.substring(0, insertAt) + prefix + html.substring(insertAt)
    }

    /** The prefix every card document gets. */
    fun prefix(): String = CSP_META
}
