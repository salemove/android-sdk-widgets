package com.glia.widgets.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class LintIssueRegistry : IssueRegistry() {
    override val issues = listOf(UndocumentedApiDetector.ISSUE)

    override val api: Int = CURRENT_API

    // works with Studio 4.1 or later; see
    // com.android.tools.lint.detector.api.Api / ApiKt
    override val minApi: Int = 8

    // Requires lint API 30.0+; if you're still building for something
    // older, just remove this property.
    override val vendor: Vendor = Vendor(
        vendorName = "Glia Technologies",
        contact = "https://www.glia.com/"
    )
}
