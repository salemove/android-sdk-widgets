package com.glia.widgets.core.engagement

import com.glia.androidsdk.Operator
import com.glia.androidsdk.engagement.EngagementState
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GliaEngagementStateRepositoryTest {

    private val engagement: EngagementState = mock()
    private val operator1: Operator = mock()
    private val operator2: Operator = mock()
    private val operatorRepo: GliaOperatorRepository = mock()
    private val repo = GliaEngagementStateRepository(operatorRepo)
    private val ID1 = "1"
    private val ID2 = "2"


    @Test
    fun `mapToEngagementStateChangeEvent returns NoEngagement when engagementState null and is not ongoing engagement`() {
        assertTrue(repo.mapToEngagementStateChangeEvent(null, null) is EngagementStateEvent.NoEngagementEvent)
    }

    @Test
    fun `mapToEngagementStateChangeEvent returns NoEngagement when engagementState null and is ongoing engagement`() {
        assertTrue(repo.mapToEngagementStateChangeEvent(engagement, null) is EngagementStateEvent.EngagementOperatorConnectedEvent)
        assertTrue(repo.mapToEngagementStateChangeEvent(null, null) is EngagementStateEvent.EngagementEndedEvent)
    }

    @Test
    fun `mapToEngagementStateChangeEvent returns EngagementOngoingEvent when operator ids equal`() {
        whenever(engagement.operator).thenReturn(operator1)
        whenever(operator1.id).thenReturn(ID1)
        whenever(operator2.id).thenReturn(ID1)
        assertTrue(repo.mapToEngagementStateChangeEvent(engagement, operator2) is EngagementStateEvent.EngagementOngoingEvent)
    }

    @Test
    fun `mapToEngagementStateChangeEvent returns EngagementOperatorChangedEvent when operator ids are different`() {
        whenever(engagement.operator).thenReturn(operator1)
        whenever(operator1.id).thenReturn(ID1)
        whenever(operator2.id).thenReturn(ID2)
        assertTrue(repo.mapToEngagementStateChangeEvent(engagement, operator2) is EngagementStateEvent.EngagementOperatorChangedEvent)
    }

    @Test
    fun `mapToEngagementStateChangeEvent returns EngagementTransferringEvent when operator ids equal`() {
        whenever(engagement.visitorStatus).thenReturn(EngagementState.VisitorStatus.TRANSFERRING)
        assertTrue(repo.mapToEngagementStateChangeEvent(engagement, null) is EngagementStateEvent.EngagementTransferringEvent)
    }
}
