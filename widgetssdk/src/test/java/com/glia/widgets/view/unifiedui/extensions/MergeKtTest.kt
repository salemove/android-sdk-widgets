package com.glia.widgets.view.unifiedui.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MergeKtTest {

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

    @Test(expected = UnsupportedOperationException::class)
    fun `deepMerge throws exception when called on non data class type`() {
        val class1: Any? = null
        val class2 = Any()

        class1 deepMerge class2
    }

    @Test
    fun `deepMerge takes non data class values from the right property when it is not null`() {
        val left = User(
            name = "name_left",
            age = 17,
            address = Address(street = "street_left", zip = "654321"),
            gender = Gender.FEMALE,
            map = mapOf("1" to "2"),
            list = listOf(1, 2, 3)
        )
        val right = User(
            name = "name",
            age = 18,
            address = Address(street = "street", zip = "123456"),
            gender = Gender.MALE,
            map = mapOf("2" to "3"),
            list = listOf(2, 3, 4)
        )

        val result = left deepMerge right

        assertEquals(result, right)
    }

    @Test
    fun `deepMerge takes non data class values from the left property when it is null on the right`() {
        val left = User(
            name = "name_left",
            age = 17,
            address = Address(street = "street_left", zip = "654321"),
            gender = Gender.FEMALE,
            map = mapOf("1" to "2")
        )
        val right = User(
            age = 18,
            address = Address(street = "street"),
            gender = Gender.MALE
        )

        val result = left deepMerge right

        assertEquals(result.name, left.name)
        assertEquals(result.address?.zip, left.address?.zip)
        assertEquals(result.map!!["1"], "2")
    }
    @Test
    fun `deepMerge deep merges data class properties`() {
        val left = User(
            name = "name_left",
            age = 17,
            address = Address(street = "street_left", zip = "654321"),
            gender = Gender.FEMALE,
            map = mapOf("1" to "2")
        )
        val right = User(
            age = 18,
            address = Address(street = "street"),
            gender = Gender.MALE
        )

        val result = left deepMerge right

        assertNotEquals(result.address, left.address)
        assertNotEquals(result.address, right.address)
        assertEquals(result.address?.street, right.address?.street)
        assertEquals(result.address?.zip, left.address?.zip)
    }
}
