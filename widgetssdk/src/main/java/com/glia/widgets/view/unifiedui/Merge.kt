package com.glia.widgets.view.unifiedui

/**
 * Interface for classes that can be merged with another instance of the same class
 */
internal interface Mergeable<T : Any> {
    /**
     * The other should always have priority over the current object
     */
    infix fun merge(other: T): T
}

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
 * Example usage:
 *
 *     val a = MyDataClass(...)
 *     val b = MyDataClass(...)
 *     val c = a merge b
 */
internal infix fun <T : Any?> T.merge(other: T): T {
    if (this == null) return other
    return nullSafeMerge(other)
}

@Suppress("UNCHECKED_CAST")
internal infix fun <T : Any> T.nullSafeMerge(other: T?): T = when {
    other == null -> this
    this is Mergeable<*> -> (this as Mergeable<T>).merge(other)
    else -> other
}

/**
 * `Checks that at least one 'non-null' item preset
 */
internal fun atLeastOneNotNull(vararg items: Any?): Boolean = items.any { it != null }

/**
 * Will invoke [composer] when at least on of [items] is non-null otherwise will return 'null'
 */
internal fun <T : Any?> composeIfAtLeastOneNotNull(vararg items: Any?, composer: () -> T): T? =
    composer.takeIf { atLeastOneNotNull(items) }?.invoke()
