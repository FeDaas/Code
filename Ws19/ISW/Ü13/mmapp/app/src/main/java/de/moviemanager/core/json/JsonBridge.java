package de.moviemanager.core.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import de.moviemanager.core.storage.JsonAttr;
import de.util.Pair;

import static de.util.ObjectUtils.getAllFields;
import static java.util.stream.Collectors.toList;

public final class JsonBridge {
    private JsonBridge() {}

    public static Optional<JSONObject> toJson(final Object o) {
        try {
            return Optional.of(tryWrapInJson(o));
        } catch(Exception e) {
            return Optional.empty();
        }

    }

    private static JSONObject tryWrapInJson(final Object o)
            throws IllegalAccessException, JSONException {
        Class<?> clazz = o.getClass();
        List<Field> fields = getAllFields(clazz)
                .stream()
                .filter(f -> f.isAnnotationPresent(JsonAttr.class))
                .collect(toList());
        fields.forEach(f -> f.setAccessible(true));

        final JSONObject obj = new JSONObject();

        for (Field f : fields) {
            Object content = f.get(o);
            if(content == null)
                obj.put(f.getName(), JSONObject.NULL);
            else if(content instanceof List)
                obj.put(f.getName(), convertList((List) content));
            else
                obj.put(f.getName(), content);
        }
        return obj;
    }

    private static JSONArray convertList(final List<?> li) {
        JSONArray array = new JSONArray();
        for(Object o : li) {
            if(o instanceof Pair)
                array.put(((Pair) o).toJsonObject());
            else
                array.put(o);
        }
        return array;
    }

    public static <T> Optional<T> fromJson(final JSONObject json,
                                           final Supplier<FromJsonObject<T>> factory) {
        try {
            return Optional.of(tryBuildFromJson(json, factory));
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    private static <T> T tryBuildFromJson(final JSONObject json,
                                          final Supplier<FromJsonObject<T>> factory) {
        final FromJsonObject<T> builder = factory.get();
        final Iterator<String> iterator = json.keys();

        while(iterator.hasNext()) {
            final String str = iterator.next();
            builder.inject(str, json.opt(str));
        }
        return builder.build();
    }
}
