package de.moviemanager.ui.wiki;

import android.graphics.Bitmap;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.wiki.data.Actor;
import de.wiki.data.Film;

import static de.moviemanager.util.AndroidStringUtils.generateIdentifier;

public final class WikiStorage {
    private static final Map<String, List<JsonObject>> PAGE_QUERY_RESULTS;
    private static final Map<String, Actor> SAVED_ACTORS;
    private static final Map<String, Film> SAVED_FILMS;
    private static Bitmap bitmap;

    static {
        PAGE_QUERY_RESULTS = new ConcurrentHashMap<>();
        SAVED_ACTORS = new ConcurrentHashMap<>();
        SAVED_FILMS = new ConcurrentHashMap<>();
    }

    private WikiStorage(){}

    public static String wrapQueryResult(final List<JsonObject> result) {
        return wrap(PAGE_QUERY_RESULTS, result);
    }

    private static <T> String wrap(final Map<String, T> map, final T result) {
        final String key = generateIdentifier(map::containsKey);
        map.put(key, result);
        return key;
    }

    public static List<JsonObject> unwrapQueryResult(final String key) {
        return unwrap(PAGE_QUERY_RESULTS, key);
    }

    private static <T> T unwrap(final Map<String, T> map, final String key) {
        return map.remove(key);
    }

    public static String wrapActor(final Actor actor) {
        return wrap(SAVED_ACTORS, actor);
    }

    public static Actor unwrapActor(final String key) {
        return unwrap(SAVED_ACTORS, key);
    }

    public static String wrapFilm(final Film film) {
        return wrap(SAVED_FILMS, film);
    }

    public static Film unwrapFilm(final String key) {
        return unwrap(SAVED_FILMS, key);
    }

    public static void storeImage(final Bitmap bitmap) {
        WikiStorage.bitmap = bitmap;
    }

    public static Bitmap retrieveImage() {
        final Bitmap result = WikiStorage.bitmap;
        WikiStorage.bitmap = null;
        return result;
    }
}
