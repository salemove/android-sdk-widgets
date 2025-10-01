package com.glia.widgets.view.head

import com.glia.androidsdk.Operator
import com.glia.telemetry_lib.BubbleStates
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal object ChatHeadLogger {

    private var unreadMessagesCount: AtomicReference<String?> = AtomicReference(null)
    private val isBubbleVisible: AtomicBoolean = AtomicBoolean(false)
    private val operator: AtomicReference<Operator?> = AtomicReference(null)
    private val isEnqueuing: AtomicBoolean = AtomicBoolean(false)

    fun logChatHeadShown(bubbleType: String) {
        if (isBubbleVisible.compareAndSet(false, true)) {
            GliaLogger.i(LogEvents.BUBBLE_SHOWN, null, mapOf(EventAttribute.BubbleType to bubbleType))
        }
    }

    fun logChatHeadHidden(bubbleType: String) {
        if (isBubbleVisible.compareAndSet(true, false)) {
            GliaLogger.i(LogEvents.BUBBLE_HIDDEN, null, mapOf(EventAttribute.BubbleType to bubbleType))
        }
    }

    fun logEnqueueingStarted() {
        if (!isBubbleVisible.get()) return

        if (isEnqueuing.compareAndSet(false, true)) {
            logBubbleStateChanged(BubbleStates.ENQUEUING)
        }
    }

    fun logOnHold() {
        logBubbleStateChanged(BubbleStates.ON_HOLD)
    }

    fun logOperatorConnected(operator: Operator) {
        val currentOperator = this.operator.get()

        if (currentOperator?.id != operator.id) {
            this.operator.set(operator)
            logBubbleStateChanged(BubbleStates.OPERATOR_CONNECTED)
        }
    }

    fun reset() {
        operator.set(null)
        unreadMessagesCount.set(null)
        isEnqueuing.set(false)
    }

    fun logPositionChanged() {
        GliaLogger.i(LogEvents.BUBBLE_POSITION_CHANGED)
    }

    fun logChatHeadClicked() {
        GliaLogger.i(LogEvents.BUBBLE_CLICKED)
    }

    fun logUnreadMessageCountChanged(count: String) {
        val currentCount = unreadMessagesCount.get()
        if (currentCount == count) return

        unreadMessagesCount.set(count)

        val bubbleVisible = isBubbleVisible.get()
        if (bubbleVisible) {
            GliaLogger.i(LogEvents.BUBBLE_UNREAD_MESSAGES_CHANGED, null, mapOf(EventAttribute.MessageCount to count))
        }
    }

    private fun logBubbleStateChanged(newState: String) {
        GliaLogger.i(LogEvents.BUBBLE_STATE_CHANGED, null, mapOf(EventAttribute.BubbleState to newState))
    }

}
