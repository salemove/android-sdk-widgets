package com.glia.widgets.core.engagement

import androidx.core.util.Consumer
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.Operator.Picture
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.core.engagement.data.LocalOperator
import com.glia.widgets.di.GliaCore
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class GliaOperatorRepositoryTest {
    private lateinit var repository: GliaOperatorRepository
    private lateinit var core: GliaCore
    private lateinit var localOperator: LocalOperator
    private lateinit var operator: Operator

    @Before
    fun setUp() {
        core = Mockito.mock(GliaCore::class.java)
        repository = spy(GliaOperatorRepository(core))
        localOperator = LocalOperator("id", "name", "imageUrl")
        operator = mock()
    }

    private fun mockOperator(imageUrl: String? = localOperator.imageUrl) {
        val picture: Picture = mock()
        whenever(picture.url) doReturn Optional.ofNullable(imageUrl)
        whenever(operator.id) doReturn localOperator.id
        whenever(operator.name) doReturn localOperator.name
        whenever(operator.picture) doReturn picture
    }

    @Test
    fun `updateIfExists returns the inserted operator if it doesn't exist in cache`() {
        mockOperator()
        val newLocalOperator = repository.updateIfExists(operator)
        assertEquals(localOperator, newLocalOperator)
    }

    @Test
    fun `updateIfExists returns the updated operator if it exists in cache without image url`() {
        val imageUrl = "new_image_url"
        mockOperator(imageUrl)
        repository.putOperator(localOperator.copy(imageUrl = null))
        val newLocalOperator = repository.updateIfExists(operator)

        assertNotEquals(localOperator, newLocalOperator)
        assertEquals(newLocalOperator.imageUrl, imageUrl)
    }


    @Test
    fun `updateIfExists returns the old operator if it exists in cache with image url`() {
        val imageUrl = "new_image_url"
        mockOperator(imageUrl)
        repository.putOperator(localOperator)
        val newLocalOperator = repository.updateIfExists(operator)

        assertEquals(localOperator, newLocalOperator)
        assertEquals(newLocalOperator.imageUrl, localOperator.imageUrl)
    }

    @Test
    fun `emit updates operator and then puts it into cache`() {
        mockOperator()
        repository.emit(operator)
        verify(repository).updateIfExists(operator)
        verify(repository).putOperator(any())
    }

    @Test
    fun `operatorById returns cached operator when exists`() {
        mockOperator()
        repository.emit(operator)
        val callback: Consumer<LocalOperator?> = mock()
        repository.getOperatorById(operator.id, callback)

        verify(callback).accept(localOperator)
    }

    @Test
    fun `operatorById returns operator from API call when cached operator not exists`() {
        mockOperator()
        stubGetOperatorResponse(operator, null)
        val callback: Consumer<LocalOperator?> = mock()

        repository.getOperatorById(operator.id, callback)

        verify(callback).accept(localOperator)
    }

    @Test
    fun `operatorById returns null when cached operator not exists and API returns error`() {
        mockOperator()
        stubGetOperatorResponse(null, GliaException("", GliaException.Cause.INVALID_INPUT))
        val callback: Consumer<LocalOperator?> = mock()

        repository.getOperatorById(operator.id, callback);
        verify(callback).accept(null)
    }

    private fun stubGetOperatorResponse(operator: Operator?, exception: GliaException?) {
        doAnswer { invocation: InvocationOnMock ->
            val callback = invocation.getArgument<RequestCallback<Operator?>>(1)
            callback.onResult(operator, exception)
            callback
        }.whenever(core).getOperator(anyString(), any())
    }
}
