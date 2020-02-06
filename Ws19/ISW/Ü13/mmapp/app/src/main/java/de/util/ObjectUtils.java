package de.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public enum  ObjectUtils {
    ;

    public static <O> O typeSafeNull(Object... ignored) {
        return null;
    }

    public static List<Method> getAllMethods(final Class<?> cls) {
        final List<Method> result = new ArrayList<>(asList(cls.getDeclaredMethods()));
        Class<?> superClass = cls.getSuperclass();

        while(superClass != null && superClass != Object.class) {
            result.addAll(asList(superClass.getDeclaredMethods()));
            superClass = superClass.getSuperclass();
        }

        return result;
    }

    public static List<Field> getAllFields(final Class<?> cls) {
        final List<Field> result = new ArrayList<>(Arrays.asList(cls.getDeclaredFields()));
        Class<?> superClass = cls.getSuperclass();
        while(superClass != null && superClass != Object.class) {
            result.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }

        return result;
    }

    public static void requireAllNonNull(final Object... objects) {
        final String msg = "Expected object at position %s to be not null, but got null.";
        for(int i = 0; i < objects.length; ++i) {
            if(objects[i] == null) {
                throw new NullPointerException(format(msg, i));
            }
        }
    }

    public static boolean nonNull(Object obj) {
        return obj != null;
    }
}
