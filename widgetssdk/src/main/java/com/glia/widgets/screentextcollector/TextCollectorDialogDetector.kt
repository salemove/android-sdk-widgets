package com.glia.widgets.screentextcollector

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.getOrNull
import androidx.core.view.isVisible
import com.glia.widgets.helper.Logger
import androidx.core.view.isNotEmpty

/**
 * Handles dialog detection logic for TextCollector.
 */
internal class TextCollectorDialogDetector(val textCollectorHelper: TextCollectorHelper) {
    fun hasDialog(allRootViews: List<View>): Boolean {
        return try {
            val visibleWindowCount = allRootViews.count { it.isVisible }
            if (visibleWindowCount > 1) {
                allRootViews.forEach { rootView ->
                    if (rootView.isVisible) {
                        if (isDialogWindow(rootView)) {
                            Logger.d(TextCollector.TAG, "Dialog detected")
                            return true
                        }
                    }
                }
            }
            if (hasComposeDialog(allRootViews)) {
                Logger.d(TextCollector.TAG, "Compose dialog detected")
                return true
            }
            Logger.d(TextCollector.TAG, "No dialog detected")
            false
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception in detectDialog $e")
            false
        }
    }

    private fun hasComposeDialog(rootViews: List<View>): Boolean {
        rootViews.forEach { rootView ->
            if (rootView.isVisible) {
                val composeViews = textCollectorHelper.findComposeViews(rootView)
                composeViews.forEach { composeView ->
                    try {
                        val semanticsNode = textCollectorHelper.getComposeSemanticsNode(composeView)
                        if (semanticsNode != null && hasDialogInSemantics(semanticsNode)) {
                            return true
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return false
    }

    private fun hasDialogInSemantics(node: androidx.compose.ui.semantics.SemanticsNode): Boolean {
        try {
            val config = node.config
            config.forEach { entry ->
                val key = entry.key.toString()
                if (key.contains("Dialog", ignoreCase = true) ||
                    key.contains("IsPopup", ignoreCase = true) ||
                    key.contains("IsDialog", ignoreCase = true)
                ) {
                    return true
                }
            }
            try {
                val roleProperty = androidx.compose.ui.semantics.SemanticsProperties.Role
                val role = config.getOrNull(roleProperty)
                if (role != null) {
                    val roleString = role.toString()
                    if (roleString.contains("Dialog", ignoreCase = true)) {
                        return true
                    }
                }
            } catch (_: Exception) {
            }
            node.children.forEach { child ->
                if (hasDialogInSemantics(child)) {
                    return true
                }
            }
        } catch (e: Exception) {
            Logger.d(TextCollector.TAG, "Exception in hasDialogInSemantics $e")
        }
        return false
    }

    private fun isDialogWindow(rootView: View): Boolean {
        val className = rootView.javaClass.name
        if (className.contains("Dialog", ignoreCase = true) ||
            className.contains("Popup", ignoreCase = true) ||
            className.contains("Alert", ignoreCase = true) ||
            className.contains("PopupWindow") ||
            className.contains("DialogWrapper")
        ) {
            return true
        }
        if (hasDialogInHierarchy(rootView)) {
            return true
        }
        val composeViews = textCollectorHelper.findComposeViews(rootView)
        if (composeViews.isNotEmpty()) {
            composeViews.forEach { composeView ->
                try {
                    val semanticsNode = textCollectorHelper.getComposeSemanticsNode(composeView)
                    if (semanticsNode != null && hasDialogInSemantics(semanticsNode)) {
                        return true
                    }
                } catch (_: Exception) {
                }
            }
        }
        val layoutParams = rootView.layoutParams
        if (layoutParams != null &&
            layoutParams.width != android.view.ViewGroup.LayoutParams.MATCH_PARENT &&
            layoutParams.height != android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ) {
            if (rootView is android.view.ViewGroup && rootView.isNotEmpty()) {
                val firstChild = rootView.getChildAt(0)
                if (firstChild.javaClass.name.contains("FitWindowsFrameLayout") ||
                    firstChild.javaClass.name.contains("DialogContainer") ||
                    firstChild is ComposeView ||
                    hasDialogInHierarchy(firstChild)
                ) {
                    return true
                }
            }
        }
        if (rootView.width > 0 && rootView.height > 0) {
            val displayMetrics = rootView.context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val widthRatio = rootView.width.toFloat() / screenWidth
            val heightRatio = rootView.height.toFloat() / screenHeight
            if ((widthRatio < 0.95f || heightRatio < 0.95f) && rootView is android.view.ViewGroup && rootView.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    private fun hasDialogInHierarchy(view: View): Boolean {
        val className = view.javaClass.name
        if (className.contains("AlertDialog") ||
            className.contains("DialogTitle") ||
            className.contains("AlertController") ||
            className.contains("ButtonPanel") ||
            className.contains("ButtonBarLayout") ||
            className.contains("ScrollView") && view.parent?.javaClass?.name?.contains("Alert") == true ||
            className.contains("androidx.appcompat.app.AlertDialog") ||
            className.contains("android.app.AlertDialog") ||
            className.contains("com.google.android.material.dialog")
        ) {
            return true
        }
        try {
            if (view.id != View.NO_ID) {
                val resourceName = view.resources.getResourceEntryName(view.id)
                if (resourceName.contains("alert", ignoreCase = true) ||
                    resourceName.contains("dialog", ignoreCase = true) ||
                    resourceName.contains("parentPanel") ||
                    resourceName.contains("topPanel") ||
                    resourceName.contains("contentPanel") ||
                    resourceName.contains("customPanel") ||
                    resourceName.contains("buttonPanel") ||
                    resourceName.contains("scrollView") ||
                    resourceName.contains("title_template")
                ) {
                    return true
                }
            }
        } catch (_: Exception) {
        }
        if (view.contentDescription?.toString()?.contains("dialog", ignoreCase = true) == true ||
            view.contentDescription?.toString()?.contains("alert", ignoreCase = true) == true
        ) {
            return true
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                if (hasDialogInHierarchy(view.getChildAt(i))) {
                    return true
                }
            }
        }
        return false
    }
}
