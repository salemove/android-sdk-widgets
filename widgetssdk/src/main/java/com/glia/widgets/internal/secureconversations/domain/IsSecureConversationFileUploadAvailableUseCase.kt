package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable

/**
 * Whether Core can upload a secure conversation file now. On the interactions backend, Core uploads only
 * into an existing secure conversation, so this stays false until the visitor's first message starts one.
 */
internal class IsSecureConversationFileUploadAvailableUseCase(private val secureConversationsRepository: SecureConversationsRepository) {
    operator fun invoke(): Flowable<Boolean> = secureConversationsRepository.fileUploadAvailableObservable
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
}
