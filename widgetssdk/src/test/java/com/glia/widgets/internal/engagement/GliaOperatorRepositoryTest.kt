package com.glia.widgets.internal.engagement

import androidx.core.util.Consumer
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.Operator.Picture
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.internal.engagement.data.LocalOperator
import com.glia.widgets.di.GliaCore
import org.junit.Assert.assertEquals
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
    private lateinit var repository: GliaOperatorRepositoryImpl
    private lateinit var core: GliaCore
    private lateinit var localOperator: LocalOperator
    private lateinit var operator: Operator

    @Before
    fun setUp() {
        core = Mockito.mock(GliaCore::class.java)
        repository = spy(GliaOperatorRepositoryImpl(core))
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
    fun `mapOperator return operator with operatorDefaultImageUrl when operator image is null`() {
        mockOperator(null)
        val operatorDefaultImageURL = "default_image"
        repository.updateOperatorDefaultImageUrl(operatorDefaultImageURL)
        val newOperator = repository.mapOperator(operator)
        assertEquals(newOperator.imageUrl, operatorDefaultImageURL)
        assertNotEquals(localOperator, newOperator)
        assertEquals(localOperator.id, newOperator.id)
        assertEquals(localOperator.name, newOperator.name)
    }

    @Test
    fun `emit updates operator and then puts it into cache`() {
        mockOperator()
        repository.emit(operator)
        verify(repository).mapOperator(operator)
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

        repository.getOperatorById(operator.id, callback)
        verify(callback).accept(null)
    }

    @Test
    fun `getOperatorImage returns default image url when operator image null and isAlwaysUseDefaultOperatorPicture true`() {
        repository.operatorDefaultImageUrl = "default"
        repository.isAlwaysUseDefaultOperatorPicture = true

        assertEquals("default", repository.getOperatorImage(null))
    }

    @Test
    fun `getOperatorImage returns default image url when operator image null and isAlwaysUseDefaultOperatorPicture false`() {
        repository.operatorDefaultImageUrl = "default"
        repository.isAlwaysUseDefaultOperatorPicture = false

        assertEquals("default", repository.getOperatorImage(null))
    }

    @Test
    fun `getOperatorImage returns default image url when operator image exists and isAlwaysUseDefaultOperatorPicture true`() {
        repository.operatorDefaultImageUrl = "default"
        repository.isAlwaysUseDefaultOperatorPicture = true

        assertEquals("default", repository.getOperatorImage(null))
    }

    @Test
    fun `getOperatorImage returns image Url when operator image exists and isAlwaysUseDefaultOperatorPicture false`() {
        repository.operatorDefaultImageUrl = "default"
        repository.isAlwaysUseDefaultOperatorPicture = false

        assertEquals("imageUrl", repository.getOperatorImage("imageUrl"))
    }

    private fun stubGetOperatorResponse(operator: Operator?, exception: GliaException?) {
        doAnswer { invocation: InvocationOnMock ->
            invocation.getArgument<RequestCallback<Operator?>>(1).apply { onResult(operator, exception) }
        }.whenever(core).getOperator(anyString(), any())
    }
}
