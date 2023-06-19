package com.glia.widgets.core.mediaupgradeoffer.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.permissions.Permissions
import com.glia.widgets.permissions.PermissionsGrantedCallback
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AcceptMediaUpgradeOfferUseCaseTest {
    private lateinit var mediaUpgradeOfferRepository: MediaUpgradeOfferRepository
    private lateinit var permissionManager: PermissionManager

    private lateinit var useCase: AcceptMediaUpgradeOfferUseCase

    @Before
    fun setUp() {
        mediaUpgradeOfferRepository = mock()
        permissionManager = mock()

        useCase = AcceptMediaUpgradeOfferUseCase(mediaUpgradeOfferRepository, permissionManager)
    }

    @Test
    fun `invoke gets permissions from permission manager`() {
        val offer = mock<MediaUpgradeOffer>()
        whenever(permissionManager.getPermissionsForMediaUpgradeOffer(any())).thenReturn(mock())

        useCase.invoke(offer, mock())

        verify(permissionManager).getPermissionsForMediaUpgradeOffer(offer)
    }

    @Test
    fun `invoke calls permission manager to request given permissions`() {
        val necessaryPermissions = listOf("NecessaryPermission")
        val additionalPermissions = listOf("AdditionalPermission")
        whenever(permissionManager.getPermissionsForMediaUpgradeOffer(any())).thenReturn(Permissions(necessaryPermissions, additionalPermissions))

        useCase.invoke(mock(), mock())

        verify(permissionManager).handlePermissions(eq(necessaryPermissions), eq(additionalPermissions), any(), eq(null), eq(null))
    }

    @Test
    fun `invoke calls media upgrade offer repository when permissions manager returns results`() {
        val necessaryPermissions = listOf("NecessaryPermission")
        val additionalPermissions = listOf("AdditionalPermission")
        val offer = mock<MediaUpgradeOffer>()
        val submitter = MediaUpgradeOfferRepository.Submitter.CHAT
        whenever(permissionManager.getPermissionsForMediaUpgradeOffer(any())).thenReturn(Permissions(necessaryPermissions, additionalPermissions))

        useCase.invoke(offer, submitter)
        val argumentCaptor = argumentCaptor<PermissionsGrantedCallback>()
        verify(permissionManager).handlePermissions(any(), any(), argumentCaptor.capture(), eq(null), eq(null))
        argumentCaptor.firstValue.invoke(true)

        verify(mediaUpgradeOfferRepository).acceptOffer(offer, submitter)
    }
}
