package de.moviemanager.util.autobind;

import android.app.Activity;
import android.view.View;

import androidx.annotation.IdRes;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public enum AutoBind {
    ;

    public static void bindAll(Object obj, View source) {
        bindAll(obj, source::findViewById);
    }

    public static void bindAll(Object obj, final IntFunction<View> findViewById) {
        bindAll(obj.getClass(), obj, findViewById);
    }

    public static void bindAll(Class<?> cls, Object obj, final IntFunction<View> findViewById) {
        List<Field> fields = Arrays.stream(cls.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Bind.class))
                .collect(Collectors.toList());
        while (cls != Object.class && fields.isEmpty()) {
            cls = cls.getSuperclass();
            if (cls == null) {
                throw new IllegalStateException("No bindable fields found!");
            }
            fields = Arrays.stream(cls.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Bind.class))
                    .collect(Collectors.toList());
        }

        if (fields.isEmpty()) {
            throw new IllegalStateException("No bindable fields found!");
        }

        for (final Field field : fields) {
            if (field.isAnnotationPresent(Bind.class)) {
                bindField(field, obj, findViewById);
            }
        }

    }

    private static void bindField(final Field field,
                                  final Object obj,
                                  final IntFunction<View> findViewById) {
        field.setAccessible(true);
        try {
            @IdRes final int id = field.getAnnotation(Bind.class).value();
            final View view = findViewById.apply(id);
            if (view == null) {
                throw new NullPointerException("No view found for: "
                        + obj.getClass().getCanonicalName()
                        + "#"
                        + field.getName());
            }
            field.set(obj, view);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void bindAll(final Activity activity) {
        bindAll(activity, activity::findViewById);
    }

    public static void bindAll(final View source) {
        bindAll(source, source::findViewById);
    }
}
