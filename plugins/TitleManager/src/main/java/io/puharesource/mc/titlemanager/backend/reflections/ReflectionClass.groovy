package io.puharesource.mc.titlemanager.backend.reflections

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

final class ReflectionClass {
    final Class<?> handle
    private final String path

    ReflectionClass(String path) throws ClassNotFoundException {
        handle = Class.forName(path)
        this.path = path
    }

    Method getMethod(String methodName, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Method method : handle.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterTypes().length != params.length) continue
            for (int i = 0; method.getParameterTypes().length > i; i++)
                if (method.getParameterTypes()[i] != params[i]) continue main
            return method
        }
        throw new NoSuchMethodException("Couldn't find method \"" + methodName + "\" for " + handle.getName() + ".")
    }

    Method getDeclaredMethod(String methodName, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Method method : handle.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterTypes().length != params.length) continue
            for (int i = 0; method.getParameterTypes().length > i; i++)
                if (method.getParameterTypes()[i] != params[i]) continue main
            method.setAccessible(true)
            return method
        }
        throw new NoSuchMethodException("Couldn't find declared method \"" + methodName + "\" for " + handle.getName() + ".")
    }

    Constructor getConstructor(Class<?>... params) throws NoSuchMethodException {
        main:
        for (Constructor constructor : handle.getConstructors()) {
            if (constructor.getParameterTypes().length != params.length) continue
            for (int i = 0; constructor.getParameterTypes().length > i; i++)
                if (constructor.getParameterTypes()[i] != params[i]) continue main
            return constructor
        }
        throw new NoSuchMethodException("Couldn't find constructor for " + handle.getName() + ".")
    }

    Object createInstance(Object... objects) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?>[] classes = new Class<?>[objects.length]

        for (int i = 0; objects.length > i; i++) {
            classes[i] = objects[i].getClass()
        }

        return getConstructor(classes).newInstance(objects)
    }

    Class<?> getInnerClass(String className) throws ClassNotFoundException {
        return Class.forName(path + '$' + className)
    }

    Class<?> getInnerReflectionClass(String className) throws ClassNotFoundException {
        new ReflectionClass(path + '$' + className)
    }

    Field getField(String name) {
        handle.getField(name)
    }
}
