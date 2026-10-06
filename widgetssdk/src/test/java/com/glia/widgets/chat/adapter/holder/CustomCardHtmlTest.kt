package com.glia.widgets.chat.adapter.holder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomCardHtmlTest {

    private val prefix: String = "<!--prefix-->"

    @Test
    fun `prefix goes first when there is no doctype`() {
        assertEquals("$prefix<p>hi</p>", CustomCardHtml.withPrefix("<p>hi</p>", prefix))
    }

    @Test
    fun `prefix goes right after a leading doctype`() {
        listOf("<!DOCTYPE html>", "<!doctype HTML>", "\n  <!DOCTYPE html>").forEach { doctype ->
            assertEquals(doctype, "$doctype$prefix<p>hi</p>", CustomCardHtml.withPrefix("$doctype<p>hi</p>", prefix))
        }
    }

    @Test
    fun `prefix goes after a doctype preceded by a comment`() {
        val html = "<!-- c --><!DOCTYPE html><p>hi</p>"

        assertEquals("<!-- c --><!DOCTYPE html>$prefix<p>hi</p>", CustomCardHtml.withPrefix(html, prefix))
    }

    @Test
    fun `prefix goes after a doctype preceded by a byte order mark`() {
        val html = "﻿<!DOCTYPE html><p>hi</p>"

        assertEquals("﻿<!DOCTYPE html>$prefix<p>hi</p>", CustomCardHtml.withPrefix(html, prefix))
    }

    @Test
    fun `prefix goes first when the doctype is not leading`() {
        val html = "<p>hi</p><!DOCTYPE html>"

        assertEquals("$prefix$html", CustomCardHtml.withPrefix(html, prefix))
    }

    @Test
    fun `prefix blocks frames and form submissions`() {
        val cardPrefix = CustomCardHtml.prefix()

        assertTrue(cardPrefix.contains("http-equiv=\"Content-Security-Policy\""))
        assertTrue(cardPrefix.contains("frame-src 'none'"))
        assertTrue(cardPrefix.contains("form-action 'none'"))
    }
}
