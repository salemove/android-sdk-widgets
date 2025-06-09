package com.glia.widgets.internal.authentication

import android.mock
import android.unMock
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.push.notifications.PushClickHandlerController
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.glia.androidsdk.visitor.Authentication as CoreAuthentication

internal class AuthenticationManagerTest {

    private lateinit var coreAuthentication: CoreAuthentication
    private lateinit var onAuthenticationRequestCallback: () -> Unit

    private lateinit var secureConversationsRepository: SecureConversationsRepository

    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var pushClickHandlerController: PushClickHandlerController


    @Before
    fun setUp() {
        coreAuthentication = mockk(relaxUnitFun = true)
        onAuthenticationRequestCallback = mockk<() -> Unit>(relaxed = true)
        secureConversationsRepository = mockk(relaxUnitFun = true)
        pushClickHandlerController = mockk(relaxUnitFun = true)
        Logger.mock()
        Dependencies.mock()

        every { Dependencies.repositoryFactory.secureConversationsRepository } returns secureConversationsRepository
        every { Dependencies.controllerFactory.pushClickHandlerController } returns pushClickHandlerController

        authenticationManager = AuthenticationManager(coreAuthentication, onAuthenticationRequestCallback)
    }

    @After
    fun tearDown() {
        Logger.unMock()
        Dependencies.unMock()
    }

    @Test
    fun `isAuthenticated calls corresponding function from core`() {
        every { coreAuthentication.isAuthenticated } returns true
        assertTrue(authenticationManager.isAuthenticated)
        verify { coreAuthentication.isAuthenticated }
    }

    @Test
    fun `setBehavior calls corresponding core function`() {
        authenticationManager.setBehavior(Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT)
        verify { coreAuthentication.setBehavior(eq(CoreAuthentication.Behavior.ALLOWED_DURING_ENGAGEMENT)) }
    }

    @Test
    fun `authenticate triggers only the success callback when core authentication succeeds`() {
        every { Dependencies.destroyControllersAndResetQueueing() } just Runs
        val token = "just token"
        val externalAccessToken = "external"
        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.authenticate(token, externalAccessToken, onComplete, onError)

        verify { onAuthenticationRequestCallback() }
        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.authenticate(eq(token), eq(externalAccessToken), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback

        //success
        verify(exactly = 0) { pushClickHandlerController.onAuthenticationAttempt() }
        verify(exactly = 0) { Dependencies.destroyControllersAndResetQueueing() }
        verify(exactly = 0) { secureConversationsRepository.subscribe() }
        verify(exactly = 0) { onComplete.onComplete() }

        //error
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, null)

        verify { pushClickHandlerController.onAuthenticationAttempt() }

        verify { Dependencies.destroyControllersAndResetQueueing() }
        verify { secureConversationsRepository.subscribe() }
        verify { onComplete.onComplete() }

        verify(exactly = 0) { onError.onError(any()) }
    }

    @Test
    fun `authenticate triggers only the error callback when core authentication fails`() {
        every { Dependencies.destroyControllersAndResetQueueing() } just Runs
        val token = "just token"
        val externalAccessToken = "external"
        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.authenticate(token, externalAccessToken, onComplete, onError)

        verify { onAuthenticationRequestCallback() }
        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.authenticate(eq(token), eq(externalAccessToken), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback

        //success
        verify(exactly = 0) { pushClickHandlerController.onAuthenticationAttempt() }
        verify(exactly = 0) { Dependencies.destroyControllersAndResetQueueing() }
        verify(exactly = 0) { secureConversationsRepository.subscribe() }
        verify(exactly = 0) { onComplete.onComplete() }

        //error
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, GliaException("error", GliaException.Cause.INVALID_INPUT))

        verify(inverse = true) { Dependencies.destroyControllersAndResetQueueing() }
        verify(exactly = 0) { secureConversationsRepository.subscribe() }
        verify(exactly = 0) { onComplete.onComplete() }

        verify { pushClickHandlerController.onAuthenticationAttempt() }
        verify { onError.onError(any()) }
    }

    @Test
    fun `deauthenticate triggers success callback only when the core deauthentication succeeds`() {
        val stopPushNotifications = true
        every { Dependencies.destroyControllersAndResetEngagementData() } just Runs

        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.deauthenticate(stopPushNotifications, onComplete, onError)

        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.deauthenticate(eq(stopPushNotifications), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback
        verify(exactly = 0) { Dependencies.destroyControllersAndResetEngagementData() }
        verify(exactly = 0) { secureConversationsRepository.unsubscribeAndResetData() }
        verify(exactly = 0) { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, null)

        verify { Dependencies.destroyControllersAndResetEngagementData() }
        verify { secureConversationsRepository.unsubscribeAndResetData() }
        verify { onComplete.onComplete() }

        verify(exactly = 0) { onError.onError(any()) }
    }

    @Test
    fun `deauthenticate triggers error callback only when the core deauthentication fails`() {
        val stopPushNotifications = true
        every { Dependencies.destroyControllersAndResetEngagementData() } just Runs

        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.deauthenticate(stopPushNotifications, onComplete, onError)

        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.deauthenticate(eq(stopPushNotifications), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback
        verify(exactly = 0) { Dependencies.destroyControllersAndResetEngagementData() }
        verify(exactly = 0) { secureConversationsRepository.unsubscribeAndResetData() }
        verify(exactly = 0) { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, GliaException("error", GliaException.Cause.INVALID_INPUT))

        verify(inverse = true) { Dependencies.destroyControllersAndResetEngagementData() }
        verify(exactly = 0) { secureConversationsRepository.unsubscribeAndResetData() }
        verify(exactly = 0) { onComplete.onComplete() }

        verify(exactly = 1) { onError.onError(any()) }
    }

    @Test
    fun `refresh triggers only the success callback when core refresh succeeds`() {
        every { Dependencies.destroyControllersAndResetQueueing() } just Runs
        val jwtToken = "just token"
        val externalAccessToken = "external"

        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.refresh(jwtToken, externalAccessToken, onComplete, onError)

        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.refresh(eq(jwtToken), eq(externalAccessToken), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback
        verify(exactly = 0) { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, null)

        verify { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }
    }

    @Test
    fun `refresh triggers only the error callback when core refresh fails`() {
        every { Dependencies.destroyControllersAndResetQueueing() } just Runs
        val jwtToken = "just token"
        val externalAccessToken = "external"

        val onComplete = mockk<OnComplete>(relaxUnitFun = true)
        val onError = mockk<OnError>(relaxUnitFun = true)
        val authCallbackSlot = slot<RequestCallback<Void>>()

        authenticationManager.refresh(jwtToken, externalAccessToken, onComplete, onError)

        verify { Logger.i(any(), any()) }
        verify {
            coreAuthentication.refresh(eq(jwtToken), eq(externalAccessToken), capture(authCallbackSlot))
        }

        //verify that all the business logic is inside callback
        verify(exactly = 0) { onComplete.onComplete() }
        verify(exactly = 0) { onError.onError(any()) }

        authCallbackSlot.captured.onResult(null, GliaException("error", GliaException.Cause.INVALID_INPUT))

        verify(exactly = 0) { onComplete.onComplete() }
        verify(exactly = 1) { onError.onError(any()) }
    }

}
