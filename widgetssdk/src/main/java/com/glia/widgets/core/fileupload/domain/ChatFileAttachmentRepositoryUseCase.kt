package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface ChatFileAttachmentRepositoryUseCase {
    operator fun invoke(): FileAttachmentRepository
    fun flowable(): Flowable<FileAttachmentRepository>
}

internal class ChatFileAttachmentRepositoryUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val engagementFileAttachmentRepository: FileAttachmentRepository,
    private val secureFileAttachmentRepository: FileAttachmentRepository
) : ChatFileAttachmentRepositoryUseCase {

    override fun invoke(): FileAttachmentRepository = when {
        engagementRepository.isQueueingOrLiveEngagement -> engagementFileAttachmentRepository
        else -> secureFileAttachmentRepository
    }

    override fun flowable() = engagementRepository.engagementState
        .map { state ->
            when {
                state.isQueueing || state.isLiveEngagement -> engagementFileAttachmentRepository
                else -> secureFileAttachmentRepository
            }
        }
        .distinctUntilChanged()
}
