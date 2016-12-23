package io.puharesource.mc.titlemanager.extensions

import java.lang.reflect.Field

internal fun Field.modify(body: Field.() -> Unit) {
    if (isAccessible) {
        body()
    } else {
        isAccessible = true
        body()
        isAccessible = false
    }
}