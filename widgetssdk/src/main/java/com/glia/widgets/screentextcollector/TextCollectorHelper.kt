package com.glia.widgets.screentextcollector

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.glia.widgets.helper.Logger

/**
 * Helper for extracting text from Compose and XML view hierarchies.
 */
internal class TextCollectorHelper() {
    fun collectTextFromViewHierarchy(view: View): List<String> {
        val texts = mutableListOf<String>()
        if (view.visibility != View.VISIBLE) return texts
        if (view is TextView) {
            view.text?.toString()?.let { text -> if (text.isNotBlank()) texts.add(text) }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                texts.addAll(collectTextFromViewHierarchy(view.getChildAt(i)))
            }
        }
        return texts
    }

    fun collectTextFromCompose(rootView: View): List<String> {
        val texts = mutableListOf<String>()
        val composeViews = findComposeViews(rootView)
        composeViews.forEach { composeView ->
            try {
                val semanticsNode = getComposeSemanticsNode(composeView)
                semanticsNode?.let { node -> texts.addAll(extractTextFromSemantics(node)) }
            } catch (_: Exception) {
            }
        }
        return texts
    }

    fun findComposeViews(view: View): List<ComposeView> {
        val composeViews = mutableListOf<ComposeView>()
        if (view is ComposeView) composeViews.add(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                composeViews.addAll(findComposeViews(view.getChildAt(i)))
            }
        }
        return composeViews
    }

    fun getComposeSemanticsNode(composeView: ComposeView): SemanticsNode? {
        getSemanticsOwnerViaPublicApi(composeView)?.let {
            Logger.d(TextCollector.TAG, "getSemanticsOwnerViaPublicApi extracted SemanticsOwner")
            return getRootSemanticsNode(it)
        }
        getSemanticsOwnerViaReflection(composeView)?.let {
            Logger.d(TextCollector.TAG, "getSemanticsOwnerViaReflection extracted SemanticsOwner")
            return getRootSemanticsNode(it)
        }
        val semanticsNodeViaChildren = getSemanticsNodeViaChildren(composeView)
        Logger.d(TextCollector.TAG, "getSemanticsNodeViaChildren extracted SemanticsOwner: ${semanticsNodeViaChildren != null}")
        return semanticsNodeViaChildren
    }

    private fun getSemanticsOwnerViaPublicApi(composeView: ComposeView): Any? {
        try {
            val instances = composeView.javaClass.declaredFields.filter { field ->
                field.type.name.contains("ViewRootForTest") || field.type.name.contains("AndroidComposeView")
            }
            for (field in instances) {
                field.isAccessible = true
                val instance = field.get(composeView)
                if (instance != null) {
                    val methods = instance.javaClass.methods
                    val semanticsOwnerMethod = methods.find { it.name == "getSemanticsOwner" }
                    if (semanticsOwnerMethod != null) {
                        return semanticsOwnerMethod.invoke(instance)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception at getSemanticsOwnerViaPublicApi() $e")
        }
        return null
    }

    private fun getSemanticsOwnerViaReflection(composeView: ComposeView): Any? {
        try {
            for (i in 0 until composeView.childCount) {
                val child = composeView.getChildAt(i)
                if (child.javaClass.name.contains("AndroidComposeView")) {
                    val semanticsOwnerMethod = child.javaClass.methods.find { it.name == "getSemanticsOwner" }
                    if (semanticsOwnerMethod != null) {
                        return semanticsOwnerMethod.invoke(child)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception at getSemanticsOwnerViaReflection() $e")
        }
        return null
    }

    private fun getSemanticsNodeViaChildren(composeView: ComposeView): SemanticsNode? {
        try {
            for (i in 0 until composeView.childCount) {
                val child = composeView.getChildAt(i)
                if (child.javaClass.name.contains("AndroidComposeView")) {
                    val getSemantics = child.javaClass.getMethod("getSemanticsOwner")
                    val semanticsOwner = getSemantics.invoke(child)
                    return semanticsOwner?.let { getRootSemanticsNode(it) }
                }
            }
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception at getSemanticsNodeViaChildren() $e")
        }
        return null
    }

    private fun getRootSemanticsNode(semanticsOwner: Any): SemanticsNode? {
        try {
            val getRootMethod = semanticsOwner.javaClass.getMethod("getRootSemanticsNode")
            return getRootMethod.invoke(semanticsOwner) as? SemanticsNode
        } catch (exception: Exception) {
            Logger.d(TextCollector.TAG, "Exception at getRootSemanticsNode() $exception")
            try {
                val getRoot = semanticsOwner.javaClass.getMethod("getUnmergedRootSemanticsNode")
                return getRoot.invoke(semanticsOwner) as? SemanticsNode
            } catch (e: Exception) {
                Logger.d(TextCollector.TAG, "Exception at getUnmergedRootSemanticsNode() $e")
            }
        }
        return null
    }

    private fun extractTextFromSemantics(node: SemanticsNode): List<String> {
        val texts = mutableListOf<String>()
        try {
            node.config.getOrNull(SemanticsProperties.Text)?.let { textList ->
                textList.forEach { annotatedString ->
                    val text = annotatedString.text
                    if (text.isNotBlank()) texts.add(text)
                }
            }
            node.config.getOrNull(SemanticsProperties.EditableText)?.let { annotatedString ->
                val text = annotatedString.text
                if (text.isNotBlank()) texts.add(text)
            }
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.let { descriptions ->
                descriptions.forEach { desc -> if (desc.isNotBlank()) texts.add(desc) }
            }
            node.children.forEach { child -> texts.addAll(extractTextFromSemantics(child)) }
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception at extractTextFromSemantics() $e")
        }
        return texts
    }
}
