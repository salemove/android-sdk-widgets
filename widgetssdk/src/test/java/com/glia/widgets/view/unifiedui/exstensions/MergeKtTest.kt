package com.glia.widgets.view.unifiedui.exstensions

import org.junit.Assert.assertEquals
import org.junit.Test

class MergeKtTest {
    class NonDataClass(val attr: Int? = null)
    data class DataClassWithNonDataClassAttribute(val attribute: NonDataClass? = NonDataClass())


    enum class Gender {
        MALE,
        FEMALE
    }

    data class Address(val street: String? = null, val zip: String? = null)
    data class User(
        val name: String? = null,
        val age: Int? = null,
        val address: Address? = null,
        val gender: Gender? = null
    )

    @Test(expected = UnsupportedOperationException::class)
    fun `safeMerge throws exception when called on non data class type`() {
        val class1: NonDataClass? = null
        val class2: NonDataClass = NonDataClass()

        class1 safeMerge class2
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `safeMerge throws exception when one of the properties is not data class, primitive or enum`() {
        val class2: DataClassWithNonDataClassAttribute = DataClassWithNonDataClassAttribute()
        val class1: DataClassWithNonDataClassAttribute =
            DataClassWithNonDataClassAttribute(NonDataClass(1))

        class2 safeMerge class1
    }

    @Test
    fun `safeMerge takes value from right when it is not null`() {
        val left = User(
            name = "name_left",
            age = 17,
            address = Address(street = "street_left", zip = "654321"),
            gender = Gender.FEMALE
        )
        val right = User(
            name = "name",
            age = 18,
            address = Address(street = "street", zip = "123456"),
            gender = Gender.MALE
        )

        val result = left safeMerge right

        assertEquals(result, right)
    }

    @Test
    fun `safeMerge takes value from the left when it is null on the right`() {
        val left = User(
            name = "name_left",
            age = 17,
            address = Address(street = "street_left", zip = "654321"),
            gender = Gender.FEMALE
        )
        val right = User(
            age = 18,
            address = Address(street = "street"),
            gender = Gender.MALE
        )

        val result = left safeMerge right

        assertEquals(result.name, left.name)
        assertEquals(result.address?.zip, left.address?.zip)
    }
}