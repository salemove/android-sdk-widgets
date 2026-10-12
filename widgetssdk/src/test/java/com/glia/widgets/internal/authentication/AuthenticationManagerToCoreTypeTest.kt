package com.glia.widgets.internal.authentication

import com.glia.androidsdk.GliaException
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import com.glia.androidsdk.visitor.Authentication as CoreAuthentication

class AuthenticationManagerToCoreTypeTest {
    private val widgetAuthentication: AuthenticationManager = mock()
    private val coreAuthentication: CoreAuthentication = widgetAuthentication.toCoreType()

    private var successCount: Int = 0
    private val failures: MutableList<GliaException> = mutableListOf()

    private val onSuccess: () -> Unit = { successCount++ }
    private val onFailure: (GliaException) -> Unit = { failures += it }

    private val onCompleteCaptor = argumentCaptor<OnComplete>()
    private val onErrorCaptor = argumentCaptor<OnError>()

    @Test
    fun toCoreType_setBehavior_delegatesMappedBehavior() {
        val behavior = CoreAuthentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT

        coreAuthentication.setBehavior(behavior)

        verify(widgetAuthentication).setBehavior(behavior.toWidgetsType())
    }

    @Test
    fun toCoreType_isAuthenticated_returnsValueFromWidgetAuthentication() {
        whenever(widgetAuthentication.isAuthenticated).thenReturn(true)

        assertTrue(coreAuthentication.isAuthenticated)
    }

    @Test
    fun toCoreType_authenticate_routesTokensAndSuccessToOnSuccess() {
        coreAuthentication.authenticate(JWT_TOKEN, EXTERNAL_TOKEN, onSuccess, onFailure)

        verify(widgetAuthentication).authenticate(eq(JWT_TOKEN), eq(EXTERNAL_TOKEN), onCompleteCaptor.capture(), onErrorCaptor.capture())
        assertOutcome(expectedSuccesses = 0)

        onCompleteCaptor.firstValue.onComplete()

        assertOutcome(expectedSuccesses = 1)
    }

    @Test
    fun toCoreType_authenticate_routesFailureToOnFailureAsCoreException() {
        coreAuthentication.authenticate(JWT_TOKEN, EXTERNAL_TOKEN, onSuccess, onFailure)

        verify(widgetAuthentication).authenticate(eq(JWT_TOKEN), eq(EXTERNAL_TOKEN), onCompleteCaptor.capture(), onErrorCaptor.capture())
        onErrorCaptor.firstValue.onError(GliaWidgetsException(ERROR_MESSAGE, GliaWidgetsException.Cause.AUTHENTICATION_ERROR))

        assertOutcome(expectedSuccesses = 0, expectedFailure = ERROR_MESSAGE to GliaException.Cause.AUTHENTICATION_ERROR)
    }

    @Test
    fun toCoreType_authenticateWithBlankToken_reportsInvalidInputWithoutCallingWidgetAuthentication() {
        coreAuthentication.authenticate(" ", EXTERNAL_TOKEN, onSuccess, onFailure)

        verifyNoInteractions(widgetAuthentication)
        assertOutcome(expectedSuccesses = 0, expectedFailure = INVALID_TOKEN_MESSAGE to GliaException.Cause.INVALID_INPUT)
    }

    @Test
    fun toCoreType_deauthenticate_routesStopPushNotificationsAndSuccessToOnSuccess() {
        coreAuthentication.deauthenticate(true, onSuccess, onFailure)

        verify(widgetAuthentication).deauthenticate(eq(true), onCompleteCaptor.capture(), onErrorCaptor.capture())
        assertOutcome(expectedSuccesses = 0)

        onCompleteCaptor.firstValue.onComplete()

        assertOutcome(expectedSuccesses = 1)
    }

    @Test
    fun toCoreType_deauthenticate_routesFailureToOnFailureAsCoreException() {
        coreAuthentication.deauthenticate(false, onSuccess, onFailure)

        verify(widgetAuthentication).deauthenticate(eq(false), onCompleteCaptor.capture(), onErrorCaptor.capture())
        onErrorCaptor.firstValue.onError(GliaWidgetsException(ERROR_MESSAGE, GliaWidgetsException.Cause.FORBIDDEN))

        assertOutcome(expectedSuccesses = 0, expectedFailure = ERROR_MESSAGE to GliaException.Cause.FORBIDDEN)
    }

    @Test
    fun toCoreType_deauthenticateWithoutStopPushNotifications_delegatesToTheWidgetDefault() {
        coreAuthentication.deauthenticate(onSuccess, onFailure)

        verify(widgetAuthentication).deauthenticate(onCompleteCaptor.capture(), onErrorCaptor.capture())

        onCompleteCaptor.firstValue.onComplete()

        assertOutcome(expectedSuccesses = 1)
    }

    @Test
    fun toCoreType_refresh_routesTokensAndSuccessToOnSuccess() {
        coreAuthentication.refresh(JWT_TOKEN, EXTERNAL_TOKEN, onSuccess, onFailure)

        verify(widgetAuthentication).refresh(eq(JWT_TOKEN), eq(EXTERNAL_TOKEN), onCompleteCaptor.capture(), onErrorCaptor.capture())
        assertOutcome(expectedSuccesses = 0)

        onCompleteCaptor.firstValue.onComplete()

        assertOutcome(expectedSuccesses = 1)
    }

    @Test
    fun toCoreType_refresh_routesFailureToOnFailureAsCoreException() {
        coreAuthentication.refresh(JWT_TOKEN, EXTERNAL_TOKEN, onSuccess, onFailure)

        verify(widgetAuthentication).refresh(eq(JWT_TOKEN), eq(EXTERNAL_TOKEN), onCompleteCaptor.capture(), onErrorCaptor.capture())
        onErrorCaptor.firstValue.onError(GliaWidgetsException(ERROR_MESSAGE, GliaWidgetsException.Cause.NETWORK_TIMEOUT))

        assertOutcome(expectedSuccesses = 0, expectedFailure = ERROR_MESSAGE to GliaException.Cause.NETWORK_TIMEOUT)
    }

    @Test
    fun toCoreType_refreshWithBlankToken_reportsInvalidInputWithoutCallingWidgetAuthentication() {
        coreAuthentication.refresh("", EXTERNAL_TOKEN, onSuccess, onFailure)

        verifyNoInteractions(widgetAuthentication)
        assertOutcome(expectedSuccesses = 0, expectedFailure = INVALID_TOKEN_MESSAGE to GliaException.Cause.INVALID_INPUT)
    }

    @Test
    fun testWidgetsAuthenticationBehaviorsCorrespondToCoreAuthenticationBehaviors() {
        val allCoreAuthBehaviors = CoreAuthentication.Behavior.entries
        val allWidgetsAuthBehaviors = Authentication.Behavior.entries

        assertEquals(allCoreAuthBehaviors.size, allWidgetsAuthBehaviors.size)
        allWidgetsAuthBehaviors.forEachIndexed { index, item ->
            val coreBehavior = item.toCoreType()

            assertNotNull(coreBehavior)
            assertEquals(coreBehavior.name, allWidgetsAuthBehaviors[index].name)
        }
    }

    @Test
    fun testCoreAuthenticationBehaviorsCorrespondToWidgetsAuthenticationBehaviors() {
        val allCoreAuthBehaviors = CoreAuthentication.Behavior.entries
        val allWidgetsAuthBehaviors = Authentication.Behavior.entries

        assertEquals(allCoreAuthBehaviors.size, allWidgetsAuthBehaviors.size)
        allCoreAuthBehaviors.forEachIndexed { index, item ->
            val widgetsBehavior = item.toWidgetsType()

            assertNotNull(widgetsBehavior)
            assertEquals(widgetsBehavior.name, allCoreAuthBehaviors[index].name)
        }
    }

    private fun assertOutcome(expectedSuccesses: Int, expectedFailure: Pair<String, GliaException.Cause>? = null) {
        assertEquals(expectedSuccesses, successCount)
        assertEquals(
            listOfNotNull(expectedFailure),
            failures.map { it.debugMessage to it.cause }
        )
    }

    private companion object {
        const val JWT_TOKEN = "validToken"
        const val EXTERNAL_TOKEN = "externalToken"
        const val ERROR_MESSAGE = "something went wrong"
        const val INVALID_TOKEN_MESSAGE = "JWT token is not valid or empty"
    }
}
