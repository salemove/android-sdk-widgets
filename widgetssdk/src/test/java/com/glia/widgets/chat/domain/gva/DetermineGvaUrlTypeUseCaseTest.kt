package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetermineGvaUrlTypeUseCaseTest {
    private lateinit var useCase: DetermineGvaUrlTypeUseCase

    @Before
    fun setUp() {
        useCase = DetermineGvaUrlTypeUseCase()
    }

    @Test
    fun `invoke returns Phone type when url contains phone link`() {
        val buttonType = useCase("tel:+37200000000")
        assertTrue(buttonType is Gva.ButtonType.Phone)
    }

    @Test
    fun `invoke returns Email type when url contains email link`() {
        val buttonType = useCase("mailto:asdfg@sdf.com")
        assertTrue(buttonType is Gva.ButtonType.Email)
    }

    @Test
    fun `invoke returns Url type when url contains url or deep link link`() {
        val buttonType = useCase("glia://test_deep_link")
        assertTrue(buttonType is Gva.ButtonType.Url)
    }
}
