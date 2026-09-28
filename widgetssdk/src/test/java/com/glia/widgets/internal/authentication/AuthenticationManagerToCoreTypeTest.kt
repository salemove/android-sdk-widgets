package com.glia.widgets.internal.authentication

import com.glia.androidsdk.GliaException
import com.glia.widgets.authentication.Authentication
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthenticationManagerToCoreTypeTest {
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

        coreAuthentication.authenticate(jwtToken, externalAccessToken, {}, {})

        verify(widgetAuthentication).authenticate(eq(jwtToken), eq(externalAccessToken), any(), any())
    }

    @Test
    fun toCoreType_authenticateWithBlankJwtToken_reportsInvalidInputWithoutCallingWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()
        var failure: GliaException? = null

        coreAuthentication.authenticate(" ", null, {}, { failure = it })

        assertEquals(GliaException.Cause.INVALID_INPUT, failure?.cause)
        verify(widgetAuthentication, never()).authenticate(any(), anyOrNull(), any(), any())
    }

    @Test
    fun toCoreType_deauthenticate_callsDeauthenticateOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        coreAuthentication.deauthenticate({}, {})

        verify(widgetAuthentication).deauthenticate(any(), any())
    }

    @Test
    fun toCoreType_deauthenticateWithStopPushNotifications_callsDeauthenticateOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        coreAuthentication.deauthenticate(true, {}, {})

        verify(widgetAuthentication).deauthenticate(eq(true), any(), any())
    }

    @Test
    fun toCoreType_isAuthenticated_returnsCorrectValue() {
        val widgetAuthentication = mock<AuthenticationManager>()
        whenever(widgetAuthentication.isAuthenticated).thenReturn(true)

        val coreAuthentication = widgetAuthentication.toCoreType()

        assertTrue(coreAuthentication.isAuthenticated)
    }

    @Test
    fun toCoreType_refreshWithValidJwtToken_callsRefreshOnWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()

        val jwtToken = "validToken"
        val externalAccessToken = "externalToken"

        coreAuthentication.refresh(jwtToken, externalAccessToken, {}, {})

        verify(widgetAuthentication).refresh(eq(jwtToken), eq(externalAccessToken), any(), any())
    }

    @Test
    fun toCoreType_refreshWithBlankJwtToken_reportsInvalidInputWithoutCallingWidgetAuthentication() {
        val widgetAuthentication = mock<AuthenticationManager>()
        val coreAuthentication = widgetAuthentication.toCoreType()
        var failure: GliaException? = null

        coreAuthentication.refresh("", null, {}, { failure = it })

        assertEquals(GliaException.Cause.INVALID_INPUT, failure?.cause)
        verify(widgetAuthentication, never()).refresh(any(), anyOrNull(), any(), any())
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
