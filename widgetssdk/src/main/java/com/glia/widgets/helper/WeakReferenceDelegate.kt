package com.glia.widgets.helper

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class WeakReferenceDelegate<T>(initializer: () -> T? = { null }) {

    private var weakReference: WeakReference<T> = WeakReference(initializer())

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference.clear()
        weakReference = WeakReference(value)
    }
}
