package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.secureconversations.SecureConversationsRepository

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
