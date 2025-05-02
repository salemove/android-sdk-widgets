package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable

internal class HasOngoingSecureConversationUseCase(
    private val secureConversationsRepository: SecureConversationsRepository,
    private val engagementStateUseCase: EngagementStateUseCase
) {
    /**
     * @return `true` if there are pending secure conversations, unread messages, or current engagement is Transferred.
     */
    private val hasOngoingInteraction: Flowable<Boolean>
        get() = Flowable.combineLatest(
            secureConversationsRepository.pendingSecureConversationsStatusObservable,
            secureConversationsRepository.unreadMessagesCountObservable,
            engagementStateUseCase()
        ) { pendingSecureConversations, unreadMessagesCount, state ->
            !state.isLiveEngagement && !state.isQueueing && (pendingSecureConversations || unreadMessagesCount > 0 || state is State.TransferredToSecureConversation)
        }

    operator fun invoke(): Flowable<Boolean> = hasOngoingInteraction.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())

    operator fun invoke(onHasOngoingSecureConversation: () -> Unit, onNoOngoingSecureConversation: () -> Unit = {}) {
        invoke().firstOrError().unSafeSubscribe { hasOngoing ->
            if (hasOngoing) {
                onHasOngoingSecureConversation()
            } else {
                onNoOngoingSecureConversation()
            }
        }
    }

}
