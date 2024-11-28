package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.helper.rx.timeoutFirstWithDefaultUntilChanged
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

internal class HasOngoingSecureConversationUseCase(
    private val secureConversationsRepository: SecureConversationsRepository,
    /*TODO add transferred SC*/
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val core: GliaCore
) {

    /**Since we're using this property in [EntryWidget] and [EngagementLauncher] where we need the immediate UI response to the user interaction,
     * it is important to to avoid delays for the user.
     *
     * @return the `pending_secure_conversations` property from the `visitor_state` channel or `false`
     * if the current socket doesn't signal a success value within the specified 1 sec window.
     */
    private val pendingSecureConversationsStatus: Observable<Boolean>
        get() = secureConversationsRepository
            .pendingSecureConversationsStatusObservable
            /*TODO rework this timeout
             * Probably its better to keep one subscription for the entire SDK lifecycle, and start with the default values.
             * This way we will avoid unnecessary delays, replaced with default values.
             */
            .timeoutFirstWithDefaultUntilChanged(1, TimeUnit.SECONDS, false)

    /**Since we're using this property in [EntryWidget] and [EngagementLauncher] where we need the immediate UI response to the user interaction,
     * it is important to to avoid delays for the user.
     *
     * @return the `pending_secure_conversations` property from the `visitor_state` channel or [NO_UNREAD_MESSAGES]
     * if the current socket doesn't signal a success value within the specified 1 sec window.
     */
    private val unreadMessagesCountObservable: Observable<Int>
        get() = secureConversationsRepository
            .unreadMessagesCountObservable
            .timeoutFirstWithDefaultUntilChanged(1, TimeUnit.SECONDS, NO_UNREAD_MESSAGES)

    /**
     * @return `true` if there are pending secure conversations or unread messages.
     */
    private val hasOngoingInteraction: Observable<Boolean>
        get() = Observable.combineLatest(
            pendingSecureConversationsStatus,
            unreadMessagesCountObservable
        ) { pendingSecureConversations, unreadMessagesCount -> pendingSecureConversations || unreadMessagesCount > 0 }
            .onErrorReturnItem(false)

    /**
     * @return `false` if the SDK is not initialized or the visitor is not authenticated.
     */
    operator fun invoke(): Observable<Boolean> = when {
        core.isInitialized.not() -> Observable.just(false)
        isAuthenticatedUseCase().not() -> Observable.just(false)
        else -> hasOngoingInteraction
    }.observeOn(AndroidSchedulers.mainThread())

    operator fun invoke(callback: (Boolean) -> Unit) = invoke().firstOrError().unSafeSubscribe(callback)

}
