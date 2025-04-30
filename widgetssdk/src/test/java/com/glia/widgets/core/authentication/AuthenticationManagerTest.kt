package com.glia.widgets.core.authentication

import com.glia.widgets.authentication.Authentication
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class AuthenticationManagerTest {

    @Test
    fun testWidgetsAuthenticationBehaviorsCorrespondToCoreAuthenticationBehaviors() {
        val allCoreAuthBehaviors = com.glia.androidsdk.visitor.Authentication.Behavior.entries
        val allWidgetsAuthBehaviors = Authentication.Behavior.entries

        assertEquals(allCoreAuthBehaviors.size, allWidgetsAuthBehaviors.size)
        allWidgetsAuthBehaviors.forEachIndexed { index, item ->
            val coreBehavior = item.toCoreType()

            Assert.assertNotNull(coreBehavior)
            Assert.assertEquals(coreBehavior.name, allWidgetsAuthBehaviors[index].name)
        }
    }

    @Test
    fun testCoreAuthenticationBehaviorsCorrespondToWidgetsAuthenticationBehaviors() {
        val allCoreAuthBehaviors = com.glia.androidsdk.visitor.Authentication.Behavior.entries
        val allWidgetsAuthBehaviors = Authentication.Behavior.entries

        assertEquals(allCoreAuthBehaviors.size, allWidgetsAuthBehaviors.size)
        allCoreAuthBehaviors.forEachIndexed { index, item ->
            val widgetsBehavior = item.toWidgetsType()

            Assert.assertNotNull(widgetsBehavior)
            Assert.assertEquals(widgetsBehavior.name, allCoreAuthBehaviors[index].name)
        }
    }
}
