package com.ssedlacek.synchronization;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {
    private final static Map<String, Method[]> classMethods = new HashMap<>();


    public static <T> Method[] getClassMethods(Class<T> clazz) {
        if (classMethods.containsKey(clazz.getName())) {
            return classMethods.get(clazz.getName());
        }

        var methods = clazz.getMethods();

        classMethods.put(clazz.getName(), methods);

        return methods;
    }
}