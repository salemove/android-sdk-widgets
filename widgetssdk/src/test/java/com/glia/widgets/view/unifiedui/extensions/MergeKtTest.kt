package com.glia.widgets.view.unifiedui.extensions

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.nullSafeMerge
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

val mergedUser = MergeableUser(
    "merged_name",
    -1,
    Address("merged_street", "merged_zip"),
    null,
    mapOf("merged_key" to "merged_value"),
    listOf(-1)
)

enum class Gender {
    MALE,
    FEMALE
}

data class Address(val street: String? = null, val zip: String? = null)
data class User(
    val name: String? = null,
    val age: Int? = null,
    val address: Address? = null,
    val gender: Gender? = null,
    val map: Map<String, String>? = null,
    val list: List<Int>? = null
)

data class MergeableUser(
    val name: String? = null,
    val age: Int? = null,
    val address: Address? = null,
    val gender: Gender? = null,
    val map: Map<String, String>? = null,
    val list: List<Int>? = null
) : Mergeable<MergeableUser> {
    override fun merge(other: MergeableUser): MergeableUser = mergedUser
}

class MergeKtTest {

    @Before
    fun setUp() {
        mockkStatic("com.glia.widgets.view.unifiedui.MergeKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("com.glia.widgets.view.unifiedui.MergeKt")
    }

    @Test
    fun `merge returns other when this is null`() {
        val other = User()
        val user: User? = null
        val result = user merge other
        assertEquals(other, result)
    }

    @Test
    fun `merge calls nullSafeMerge when this is not null`() {
        mockkStatic("com.glia.widgets.view.unifiedui.MergeKt")
        val user = User("User")
        val other = User("Other")
        val result = user merge other

        verify { user.nullSafeMerge(other) }
        assertEquals(other, result)
    }

    @Test
    fun `nullSafeMerge returns this when other is null`() {
        val user = User("User")
        val other: User? = null
        val result = user.nullSafeMerge(other)
        assertEquals(user, result)
    }

    @Test
    fun `nullSafeMerge calls merge when this is Mergeable`() {
        val other = MergeableUser("Other")
        val user = mockk<MergeableUser>()
        every { user.merge(other) } returns mergedUser
        val result = user.nullSafeMerge(other)
        verify { user.merge(other) }
        assertEquals(mergedUser, result)
    }

    @Test
    fun `nullSafeMerge returns other when this is not Mergeable`() {
        val user = User("User")
        val other = User("Other")
        val result = user.nullSafeMerge(other)
        assertEquals(other, result)
    }
}
