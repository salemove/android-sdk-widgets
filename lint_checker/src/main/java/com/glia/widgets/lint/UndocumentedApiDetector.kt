package com.glia.widgets.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

class UndocumentedApiDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return UndocumentedApiChecker(context)
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "UndocumentedApiIssue",
            briefDescription = "Undocumented public API detected",
            explanation = """
                This lint error exists to highlight any undocumented public APIs.
                Please make sure that any API is covered in docs or is marked with @hide method
            """,
            category = Category.CORRECTNESS,
            // The priority, a number from 1 to 10 with 10 being most important/severe
            priority = 6,
            // - WARNING level will highlighted it as warning but will not fail anything unless specified otherwise in lint general settings
            // - ERROR level will highlighted it as error in Android Studio but will not fail any builds.
            // But gradle lint task will fail if it is run manually. We have this as separate step of CI so it will fail.
            // - FATAL level will fail during release builds
            severity = Severity.ERROR,
            Implementation(
                UndocumentedApiDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
