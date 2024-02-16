package com.glia.widgets.engagement.domain

import android.app.Activity
import android.content.Intent
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.ScreenSharingState
import io.reactivex.Flowable

internal interface ScreenSharingUseCase {
    val isSharing: Boolean
    operator fun invoke(): Flowable<ScreenSharingState>
    fun end()
    fun declineRequest()
    fun acceptRequest(activity: Activity, mode: ScreenSharing.Mode)
    fun acceptRequestWithAskedPermission(activity: Activity, mode: ScreenSharing.Mode)
    fun onActivityResultSkipPermissionRequest(resultCode: Int, intent: Intent?)
}

internal class ScreenSharingUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase
) : ScreenSharingUseCase {
    override val isSharing: Boolean get() = engagementRepository.isSharingScreen
    override fun invoke(): Flowable<ScreenSharingState> = engagementRepository.screenSharingState
    override fun end() {
        engagementRepository.endScreenSharing()
        removeScreenSharingNotificationUseCase()
    }

    override fun declineRequest() = engagementRepository.declineScreenSharingRequest()
    override fun acceptRequest(activity: Activity, mode: ScreenSharing.Mode) = engagementRepository.acceptScreenSharingRequest(activity, mode)
    override fun acceptRequestWithAskedPermission(activity: Activity, mode: ScreenSharing.Mode) =
        engagementRepository.acceptScreenSharingWithAskedPermission(activity, mode)

    override fun onActivityResultSkipPermissionRequest(resultCode: Int, intent: Intent?) =
        engagementRepository.onActivityResultSkipScreenSharingPermissionRequest(resultCode, intent)
}
