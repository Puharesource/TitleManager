package io.puharesource.mc.titlemanager.backend.reflections;

import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionClass {
    private final Class<?> handle;
    private final String path;

    public ReflectionClass(String path) throws ClassNotFoundException {
        handle = Class.forName(path);
        this.path = path;
    }

    public Method getMethod(String methodName, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Method method : handle.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterTypes().length != params.length) continue;
            for (int i = 0; method.getParameterTypes().length > i; i++)
                if (!method.getParameterTypes()[i].equals(params[i])) continue main;
            return method;
        }

        throw new NoSuchMethodException("Couldn't find method \"" + methodName + "\" for " + handle.getName() + ".");
    }

    public Constructor getConstructor(Class<?>... params) throws NoSuchMethodException {
        main:
        for (Constructor constructor : handle.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length != params.length) continue;
            for (int i = 0; constructor.getParameterTypes().length > i; i++)
                if (constructor.getParameterTypes()[i] != params[i]) continue main;
            return constructor;
        }

        throw new NoSuchMethodException("Couldn't find constructor for " + handle.getName() + ".");
    }

    @SneakyThrows
    public Object createInstance(Object... objects) {
        Class<?>[] classes = new Class<?>[objects.length];

        for (int i = 0; objects.length > i; i++) {
            classes[i] = objects[i].getClass();
        }

        return getConstructor(classes).newInstance(objects);
    }

    @SneakyThrows(ClassNotFoundException.class)
    public Class<?> getInnerClass(final String className) {
        return Class.forName(path + "$" + className);
    }

    @SneakyThrows(ClassNotFoundException.class)
    public ReflectionClass getInnerReflectionClass(final String className) {
        return new ReflectionClass(path + "$" + className);
    }

    @SneakyThrows(NoSuchFieldException.class)
    public Field getField(final String name) {
        return handle.getField(name);
    }

    public final Class<?> getHandle() {
        return handle;
    }
}
