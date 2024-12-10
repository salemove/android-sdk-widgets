package com.glia.widgets.core.fileupload.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface FileAttachmentRepositoryUseCase {
    operator fun invoke(): FileAttachmentRepository
    fun flowable(): Flowable<FileAttachmentRepository>
}

internal class FileAttachmentRepositoryUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val engagementFileAttachmentRepository: FileAttachmentRepository,
    private val secureFileAttachmentRepository: FileAttachmentRepository
) : FileAttachmentRepositoryUseCase {

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
