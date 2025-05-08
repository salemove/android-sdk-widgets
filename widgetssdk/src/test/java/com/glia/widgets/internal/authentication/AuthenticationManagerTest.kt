package com.glia.widgets.internal.authentication

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.authentication.Authentication
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class AuthenticationManagerTest {
    @Test
    fun toCoreType_setBehavior_returnsCoreAuthenticationWithCorrectBehavior() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val behavior = com.glia.androidsdk.visitor.Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT
        coreAuthentication.setBehavior(behavior)

        verify(widgetAuthentication).setBehavior(behavior.toWidgetsType())
    }

    @Test
    fun toCoreType_authenticateWithValidJwtToken_callsAuthenticateOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val jwtToken = "validToken"
        val externalAccessToken = "externalToken"
        val callback = mock<RequestCallback<Void>>()

        coreAuthentication.authenticate(jwtToken, externalAccessToken, callback)

        verify(widgetAuthentication).authenticate(eq(jwtToken), eq(externalAccessToken), any(), any())
    }

    @Test
    fun toCoreType_authenticateWithNullJwtToken_callsAuthCallbackWithInvalidInputError() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val callback = mock<RequestCallback<Void>>()

        coreAuthentication.authenticate(null, "externalToken", callback)

        verify(callback).onResult(
            eq(null),
            argThat { this.cause == GliaException.Cause.INVALID_INPUT }
        )
    }

    @Test
    fun toCoreType_deauthenticate_callsDeauthenticateOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val callback = mock<RequestCallback<Void>>()

        coreAuthentication.deauthenticate(callback)

        verify(widgetAuthentication).deauthenticate(any(), any())
    }

    @Test
    fun toCoreType_isAuthenticated_returnsCorrectValue() {
        val widgetAuthentication = mock<AuthenticationManager>()
        whenever(widgetAuthentication.isAuthenticated).thenReturn(true)

        val coreAuthentication = widgetAuthentication.toCoreType()

        assertTrue(coreAuthentication.isAuthenticated())
    }

    @Test
    fun toCoreType_refreshWithValidJwtToken_callsRefreshOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val jwtToken = "validToken"
        val externalAccessToken = "externalToken"
        val callback = mock<RequestCallback<Void>>()

        coreAuthentication.refresh(jwtToken, externalAccessToken, callback)

        verify(widgetAuthentication).refresh(eq(jwtToken), eq(externalAccessToken), any(), any())
    }

    @Test
    fun toCoreType_refreshWithNullJwtToken_callsAuthCallbackWithInvalidInputError() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val callback = mock<RequestCallback<Void>>()

        coreAuthentication.refresh(null, "externalToken", callback)

        verify(callback).onResult(
            eq(null),
            argThat { this is GliaException && this.cause == GliaException.Cause.INVALID_INPUT }
        )
    }

    @Test
    fun testWidgetsAuthenticationBehaviorsCorrespondToCoreAuthenticationBehaviors() {
        val allCoreAuthBehaviors = com.glia.androidsdk.visitor.Authentication.Behavior.entries
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
        val allCoreAuthBehaviors = com.glia.androidsdk.visitor.Authentication.Behavior.entries
        val allWidgetsAuthBehaviors = Authentication.Behavior.entries

        assertEquals(allCoreAuthBehaviors.size, allWidgetsAuthBehaviors.size)
        allCoreAuthBehaviors.forEachIndexed { index, item ->
            val widgetsBehavior = item.toWidgetsType()

            assertNotNull(widgetsBehavior)
            assertEquals(widgetsBehavior.name, allCoreAuthBehaviors[index].name)
        }
    }
}
