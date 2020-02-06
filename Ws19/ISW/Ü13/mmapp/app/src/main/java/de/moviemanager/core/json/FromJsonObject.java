package de.moviemanager.core.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import de.util.Pair;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public abstract class FromJsonObject<T> {
    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    private final Function<Map<String, Object>, T> createSource;
    private final Map<String, Object> objects;
    private final Map<String, BiConsumer<T, Object>> indexedSetter;

    FromJsonObject(Function<Map<String, Object>, T> createSource) {
        this.createSource = createSource;
        this.objects = new HashMap<>();
        this.indexedSetter = new HashMap<>();
    }

    <X> void registerSetter(String index, BiConsumer<T, X> setter) {
        this.indexedSetter.put(index, (t, o) -> {
            try {
                setter.accept(t, (X) o);
            } catch (Exception e) {
                //
            }
        });
    }

    void registerConversion(String index, Function<String, Object> conversion) {
        BiConsumer<T, Object> setter = indexedSetter.get(index);
        this.indexedSetter.put(index, (t, obj) -> setter.accept(t, conversion.apply(obj.toString())));
    }

    static Date convertStringToDate(String s) {
        final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    static List<String> convertJSONArrayToListOfStrings(String s) {
        try {
            JSONArray array = new JSONArray(s);
            return range(0, array.length())
                    .mapToObj(array::optString)
                    .collect(toList());
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public void inject(String index, Object o) {
        objects.put(index, o);
    }

    public T build() {
        T dis = createSource.apply(objects);
        Consumer<Pair<BiConsumer<T, Object>, Object>> activate = pair -> activateSetter(dis, pair);

        objects.entrySet()
                .stream()
                .map(Pair::paired)
                .filter(p -> indexedSetter.containsKey(p.first))
                .map(p -> p.mapFirst(indexedSetter::get))
                .forEach(activate);

        return dis;
    }

    private void activateSetter(T dis, Pair<BiConsumer<T, Object>, Object> pair) {
        BiConsumer<T, Object> setter = pair.first;
        Object object = pair.second;

        setter.accept(dis, object);
    }
}
