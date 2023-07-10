package com.glia.widgets.chat.domain.gva

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.model.Gva
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class IsGvaUseCaseTest {
    private lateinit var useCase: IsGvaUseCase
    private lateinit var getGvaTypeUseCase: GetGvaTypeUseCase
    private lateinit var chatMessage: ChatMessage

    @Before
    fun setUp() {
        chatMessage = mock()
        getGvaTypeUseCase = mock()
        useCase = IsGvaUseCase(getGvaTypeUseCase)
    }

    @Test
    fun `invoke returns true when gva type exists`() {
        val metadata = JSONObject().put(Gva.Keys.TYPE, Gva.Type.PLAIN_TEXT.value)
        whenever(chatMessage.metadata) doReturn metadata
        whenever(getGvaTypeUseCase(metadata)) doReturn Gva.Type.PLAIN_TEXT

        assertTrue(useCase(chatMessage))
    }

    @Test
    fun `invoke returns false when gva type doesn't exist`() {
        val metadata = JSONObject().put(Gva.Keys.TYPE, "asdfg")
        whenever(chatMessage.metadata) doReturn metadata
        whenever(getGvaTypeUseCase(metadata)) doReturn null

        assertFalse(useCase(chatMessage))
    }
}
