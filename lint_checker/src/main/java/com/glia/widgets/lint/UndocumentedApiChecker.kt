package com.glia.widgets.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiJavaDocumentedElement
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getAnchorPsi
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.kotlin.parentAs

// This class goes through the scope (scope is set by 'Detector' class) of UAST elements.
// I left some links that might be useful when working with this class in module-level Readme file
class UndocumentedApiChecker(private val context: JavaContext) : UElementHandler() {

    override fun visitClass(node: UClass) {
        if (isPublicApiWithoutDocs(node)) {
            reportIssue(node)
        }
    }

    override fun visitMethod(node: UMethod) {
        // I think that for now we don't need method warning. But technically current solution should work for methods as well
    }

    override fun visitField(node: UField) {
        // I think that for now we don't need fields warning. But technically current solution should work for fields as well
    }

    private fun isPublicApiWithoutDocs(element: UDeclaration): Boolean {
        if (element !is PsiJavaDocumentedElement) {
            // Not public API because this element can't have documentation element.
            return false
        }
        if (!element.isPublicWhenResolvedWithParents()) {
            // Not a public method/class/field
            return false
        }
        val docText = element.docComment?.text
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

    // Checks specifically for Kotlin 'internal' access modifier
    private fun UDeclaration.isVisibilityInternal(): Boolean {
        return getAnchorPsi()
            .parentAs<KtModifierListOwner>()
            ?.getChildOfType<KtModifierList>()
            ?.text
            ?.contains("internal") ?: false

    }

    // For some reason 'UastVisibility.PUBLIC' is returned
    // when UElement 'getVisibility()' method is used even when there is Kotlin 'internal' access modifier.
    // So adding this custom visibility checker to solve it
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
