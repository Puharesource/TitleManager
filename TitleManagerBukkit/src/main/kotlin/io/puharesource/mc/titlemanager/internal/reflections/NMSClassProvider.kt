package io.puharesource.mc.titlemanager.internal.reflections

import java.util.TreeMap

abstract class NMSClassProvider {
    private val classes: MutableMap<String, ReflectionClass> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val classChatComponentText by lazy { ChatComponentText() }

    protected fun put(path: String, clazz: ReflectionClass) = classes.put(path, clazz)

    protected fun String.associate(type: NMSType? = null, path: String, vararg inners: String) {
        var clazz = type?.getReflectionClass(path) ?: ReflectionClass(path)

        inners.forEach {
            clazz = clazz.getInnerReflectionClass(it)
        }

        put(this, clazz)
    }

    operator fun get(path: String) = classes[path]!!

    fun getIChatComponent(text: String): Any = classChatComponentText.constructor.newInstance(text)
}
