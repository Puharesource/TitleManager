package io.puharesource.mc.titlemanager.reflections

import java.util.*

abstract class NMSClassProvider {
    val classes : MutableMap<String, ReflectionClass> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    val chatComponentText : ReflectionClass by lazy { get("ChatComponentText") }

    protected fun put(path: String, clazz: ReflectionClass) = classes.put(path, clazz)
    fun get(path: String) = classes[path]!!

    fun getIChatComponent(text: String) : Any = chatComponentText
            .getConstructor(String::class.java)
            .newInstance(text)
}