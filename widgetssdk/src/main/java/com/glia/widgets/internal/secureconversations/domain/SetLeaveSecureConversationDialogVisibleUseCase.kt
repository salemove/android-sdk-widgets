package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.internal.secureconversations.SecureConversationsRepository

internal interface SetLeaveSecureConversationDialogVisibleUseCase {
    operator fun invoke(isVisible: Boolean)
}

internal class SetLeaveSecureConversationDialogVisibleUseCaseImpl(
    private val repository: SecureConversationsRepository
) : SetLeaveSecureConversationDialogVisibleUseCase {

    override operator fun invoke(isVisible: Boolean) {
        repository.setLeaveSecureConversationDialogVisible(isVisible)
    }
}
