package io.puharesource.mc.titlemanager.internal.reflections

import java.lang.reflect.Field

class ReflectionClass(private val path: String) {
    val handle: Class<*> = Class.forName(path)

    fun getMethod(methodName: String, vararg params: Class<*>) = handle.getDeclaredMethod(methodName, *params)
            ?: throw NoSuchMethodException("Couldn't find method for ${handle.name}.")

    fun getConstructor(vararg params: Class<*>) = handle.getDeclaredConstructor(*params)
            ?: throw NoSuchMethodException("Couldn't find constructor for ${handle.name}")

    fun createInstance(vararg objects: Any): Any {
        val classes: Array<Class<*>> = objects.map { it.javaClass }.toTypedArray()

        return getConstructor(*classes).newInstance(*objects)
    }

    fun getField(fieldName: String): Field = handle.getDeclaredField(fieldName)

    fun getInnerClass(className: String): Class<*> = Class.forName("$path$$className")

    fun getInnerReflectionClass(className: String) = ReflectionClass("$path$$className")
}
