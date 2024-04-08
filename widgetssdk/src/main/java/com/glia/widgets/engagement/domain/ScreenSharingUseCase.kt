package com.glia.widgets.engagement.domain

import android.app.Activity
import android.content.Intent
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.ScreenSharingState
import io.reactivex.rxjava3.core.Flowable

internal interface ScreenSharingUseCase {
    val isSharing: Boolean
    operator fun invoke(): Flowable<ScreenSharingState>
    fun end()
    fun declineRequest()
    fun acceptRequestWithAskedPermission(activity: Activity, mode: ScreenSharing.Mode)
    fun onActivityResultSkipPermissionRequest(resultCode: Int, intent: Intent?)
}

internal class ScreenSharingUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val releaseScreenSharingResourcesUseCase: ReleaseScreenSharingResourcesUseCase
) : ScreenSharingUseCase {
    override val isSharing: Boolean get() = engagementRepository.isSharingScreen
    override fun invoke(): Flowable<ScreenSharingState> = engagementRepository.screenSharingState
    override fun end() {
        engagementRepository.endScreenSharing()
        releaseScreenSharingResourcesUseCase()
    }

    override fun declineRequest() = engagementRepository.declineScreenSharingRequest()
    override fun acceptRequestWithAskedPermission(activity: Activity, mode: ScreenSharing.Mode) =
        engagementRepository.acceptScreenSharingWithAskedPermission(activity, mode)

    override fun onActivityResultSkipPermissionRequest(resultCode: Int, intent: Intent?) =
        engagementRepository.onActivityResultSkipScreenSharingPermissionRequest(resultCode, intent)
}
