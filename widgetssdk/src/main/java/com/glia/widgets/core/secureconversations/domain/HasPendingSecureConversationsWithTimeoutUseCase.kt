package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

internal class HasPendingSecureConversationsWithTimeoutUseCase(
    private val secureConversationsRepository: SecureConversationsRepository,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val core: GliaCore
) {
    /**
     * @return `false` if the SDk is not initialized, visitor is not authenticated or
     * if the current socket doesn't signal a success value within the specified 1 sec window.
     *
     * Since we're using this function in [EntryWidget] and [EngagementLauncher] where we need the immediate UI response to the user interaction,
     *  * it is important to to avoid delays for the user.
     */
    operator fun invoke(): Single<Boolean> = when {
        core.isInitialized.not() -> Single.just(false)
        isAuthenticatedUseCase().not() -> Single.just(false)
        else -> secureConversationsRepository.getHasPendingSecureConversations()
            .timeout(1, TimeUnit.SECONDS)
            .onErrorReturnItem(false)
    }
}
