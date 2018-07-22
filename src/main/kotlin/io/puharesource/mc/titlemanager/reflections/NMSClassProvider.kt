package io.puharesource.mc.titlemanager.reflections

import java.util.*

abstract class NMSClassProvider {
    private val classes : MutableMap<String, ReflectionClass> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val chatComponentText : ReflectionClass by lazy { get("ChatComponentText") }

    protected fun put(path: String, clazz: ReflectionClass) = classes.put(path, clazz)

    protected fun String.associate(type: NMSType, path: String, vararg inners: String) {
        var clazz = type.getReflectionClass(path)

        inners.forEach {
            clazz = clazz.getInnerReflectionClass(it)
        }

        put(this, clazz)
    }

    fun get(path: String) = classes[path]!!

    fun getIChatComponent(text: String) : Any = chatComponentText
            .getConstructor(String::class.java)
            .newInstance(text)
}