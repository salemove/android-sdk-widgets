package com.glia.widgets.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getAnchorPsi
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.java.JavaUClass
import org.jetbrains.uast.kotlin.KotlinUClass
import org.jetbrains.uast.kotlin.parentAs

// This class goes through the scope (scope is set by 'Detector' class) of UAST elements.
// I left some links that might be useful when working with this class in module-level Readme file
class UndocumentedApiChecker(private val context: JavaContext) : UElementHandler() {

    override fun visitClass(node: UClass) {
        if (node.getAnchorPsi() == null) {
            // This is most likely a Kotlin function that is outside of class scope.
            // For some reason they are identified as classes
            return
        }
        if (isPublicApiWithoutDocs(node)) {
            reportIssue(node)
        }
    }

    private fun isPublicApiWithoutDocs(element: UDeclaration): Boolean {
        if (!element.isPublicWhenResolvedWithParents()) {
            return false
        }
        val docText = getDocText(element)
        if (docText.isNullOrBlank()) {
            // No documentation
            return true
        } else if (docText.replace(Regex("\\W"), "").isBlank()) {
            // Empty documentation
            return true
        } else if (docText.contains("@hide")) {
            // At the moment this is a useless check, but wanted to leave it so it won't get missed in the future if we add more checks
            return false
        } else {
            return false
        }
    }

    private fun getDocText(element: UDeclaration): String? {
        if (element is JavaUClass) {
            // Returns JavaDocs content
            return element.docComment?.text
        } else if (element is KotlinUClass) {
            // Returns KDoc content
            return element.getAnchorPsi().parentAs<KtDeclaration>()?.docComment?.text
        } else {
            throw UnsupportedOperationException("This Lint checker supports only Java and Kotlin languages")
        }
    }

    // Checks specifically for Kotlin 'internal' access modifier
    private fun UDeclaration.isVisibilityInternal(): Boolean {
        return getAnchorPsi()
            .parentAs<KtModifierListOwner>()
            ?.getChildOfType<KtModifierList>()
            ?.text
            ?.contains("internal") ?: false

    }

    // When UElement 'getVisibility()' method is used with Kotlin 'internal' access modifier it returns 'UastVisibility.PUBLIC'.
    // Adding this custom visibility checker to fix this behavior.
    private fun UDeclaration.isPublic(): Boolean {
        return this.visibility == UastVisibility.PUBLIC && !this.isVisibilityInternal()
    }

    private fun UDeclaration.isPublicWhenResolvedWithParents(): Boolean {
        if (isPublic()) {
            val parentClass = this.getContainingUClass()
            return parentClass?.isPublicWhenResolvedWithParents() ?: true
        }
        return false
    }

    private fun reportIssue(node: UElement) {
        val incident = Incident(context, UndocumentedApiDetector.ISSUE)
            .message("Please make sure that all public API have docs, even deprecated! Or add a @hide keyword inside doc-comment")
            .at(node)
        context.report(incident)
    }
}
