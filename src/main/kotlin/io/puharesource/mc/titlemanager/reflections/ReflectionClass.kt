package io.puharesource.mc.titlemanager.reflections

class ReflectionClass(val path: String) {
    val handle : Class<*>

    fun getMethod(methodName: String, vararg params: Class<*>) = handle.declaredMethods
            .filter { it.name == methodName }
            .filter { it.parameterTypes.size == params.size }
            .filter { it.parameterTypes.filterIndexed { i, clazz -> clazz == params[i] }.size == params.size }
            .firstOrNull() ?: throw NoSuchMethodException("Couldn't find constructor for ${handle.name}.")

    fun getConstructor(vararg params: Class<*>) = handle.declaredConstructors
            .filter { it.parameterTypes.size == params.size }
            .filter { it.parameterTypes.filterIndexed { i, clazz -> clazz == params[i] }.size == params.size }
            .firstOrNull() ?: throw NoSuchMethodException("Couldn't find constructor for ${handle.name}.")

    fun createInstance(vararg objects: Any) : Any {
        val classes : Array<Class<*>> = objects.map { it.javaClass }.toTypedArray()

        return getConstructor(*classes).newInstance(objects)
    }

    fun getField(fieldName: String) = handle.getField(fieldName)

    fun getInnerClass(className: String) = Class.forName("$path$$className")

    fun getInnerReflectionClass(className: String) = ReflectionClass("$path$$className")

    init {
        this.handle = Class.forName(path)
    }
}