package com.glia.widgets.view.unifiedui

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Deep merge two data classes
 *
 * The resulting data class will contain:
 * - all fields of `other` which are non-null
 * - the fields of `this` for the fields which are null in `other`
 *
 * The function is immutable, the original data classes are not changed,
 * and a new data class instance is returned.
 *
 *@throws UnsupportedOperationException when called on classes different from data classes, or
 * data classes with attributes different from 'data class', 'primitive' or 'enum' type
 *
 * Example usage:
 *
 *     val a = MyDataClass(...)
 *     val b = MyDataClass(...)
 *     val c = a deepMerge b
 */
internal inline infix fun <reified T : Any?> T.deepMerge(other: T): T {
    if (!T::class.isData) throw UnsupportedOperationException("Merge supports only data classes")
    return unsafeMerge(other)
}

internal fun <T : Any?> T.unsafeMerge(other: T): T {
    if (this == null) return other
    if (other == null) return this
    return mergeNonNull(other)
}

private fun <T : Any> T.mergeNonNull(other: T): T {
    // group properties by 'name'
    val nameToProperty = this::class.declaredMemberProperties.associateBy { it.name }
    // primary constructor of a current type
    val primaryConstructor = this::class.primaryConstructor!!
    val mergedProperties: Map<KParameter, Any?> =
        mergeProperties(primaryConstructor.parameters, nameToProperty, this, other)
    return primaryConstructor.callBy(mergedProperties)
}

private fun <T : Any?> mergeProperties(
    primaryConstructorParams: List<KParameter>,
    currentClassNameToProperty: Map<String, KProperty1<out T & Any, *>>,
    current: T & Any, /* T & Any means that 'T' here is non-null */
    other: T
) = primaryConstructorParams.associateWith { parameter ->
    val property = currentClassNameToProperty[parameter.name]!!
    // current field type
    val type = property.returnType.classifier as KClass<*>

    when {
        type.isData -> mergeDataProperties(property, current, other)
        else -> mergeEndValues(property, current, other)
    }
}

/**
 * Deep merge two data properties
 */
private fun <T> mergeDataProperties(
    property: KProperty1<out T, T>,
    current: T,
    other: T
): Any? = property.getter.run {
    val currentValue: T? = call(current)
    val otherValue: T? = call(other)
    currentValue.unsafeMerge(otherValue)
}

/**
 * Deep merge two primitive properties
 */
private fun <T : Any?> mergeEndValues(
    property: KProperty1<out T, Any?>,
    current: T,
    other: T
): Any? = property.getter.run { call(other) ?: call(current) }

/**
 * `Checks that at least one 'non-null' item preset
 */
internal fun atLeastOneNotNull(vararg items: Any?): Boolean = items.any { it != null }

/**
 * Will invoke [composer] when at least on of [items] is non-null otherwise will return 'null'
 */
internal fun <T : Any?> composeIfAtLeastOneNotNull(vararg items: Any?, composer: () -> T): T? =
    composer.takeIf { atLeastOneNotNull(items) }?.invoke()
