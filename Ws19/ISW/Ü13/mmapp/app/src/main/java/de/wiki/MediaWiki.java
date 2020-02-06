package de.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import de.wiki.data.Actor;
import de.wiki.data.Film;
import de.wiki.loader.ActorLoader;
import de.wiki.loader.FilmLoader;

import static de.util.StringUtils.normedMinimumEditDistance;
import static de.wiki.query.NameBasedQuery.createQueryURL;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;

/**
 * <p>
 *     Central class for data retrieval from Wikipedia.
 *     The default wiki that is used is the
 *     <a href="https://en.wikipedia.org">English Wikipedia</a> wrapped with the
 *     {@link Wikipedia} class - a light weighted access point.
 * </p>
 *
 * <p>
 *     The data which can be received is one of following categories:
 *     <ul>
 *         <li>Actor (stored in {@link Actor})</li>
 *         <li>Film (stored in {@link Film})</li>
 *     </ul>
 *     Because of the partial name match, which will be performed during the query often
 *     more than one (possibly even without the correct) match is collected. So one can select
 *     the correct match for data retrieval.
 * </p>
 *
 * <br><b>Author</b>: <a href="mailto:schustrchr@gmail.com">Ctoffer</a><br>
 */
public final class MediaWiki {
    private MediaWiki() {

    }

    private static final FilmLoader FILM_LOADER = FilmLoader.getInstance();
    private static final ActorLoader ACTOR_LOADER = ActorLoader.getInstance();

    private static Wikipedia wikipedia;

    static {
        resetToEnglishWikipedia();
    }

    /**
     * Resets the underlying Wiki to the default english.
     * This call is analog to:
     * <pre>{@code
     * useOtherWiki(new Wikipedia("en.wikipedia.org"));
     * }</pre>
     */
    public static void resetToEnglishWikipedia() {
        useOtherWiki(new Wikipedia("en.wikipedia.org"));
    }

    /**
     * Returns the current underlying Wikipedia instance.
     *
     * @return the current underlying wiki instance
     */
    public static Wikipedia getCurrentWikipedia() {
        return wikipedia;
    }

    /**
     * Use the given Wiki as the new underlying wiki instance.
     *
     * @param wiki must be non <i>null</i>
     *
     * @throws NullPointerException if the given wiki is <i>null</i>
     */
    public static void useOtherWiki(Wikipedia wiki) {
        wikipedia = requireNonNull(wiki);
    }

    /**
     * Returns a list of possible actor Wiki pages matching the given name, sorted by similarity
     * where the first element has the greatest similarity to the given name.
     * The similarity is measured by {@link de.util.StringUtils#normedMinimumEditDistance(String, String)}
     * If an error occurred during data retrieval the Optional is empty, if only non-actor pages
     * match the given name, the list is empty.
     * To retrieve the actor data from a page see {@link MediaWiki#getActorDataFromWikiPage}
     *
     * @param name partial or complete name of the desired actor
     * @return Optional is empty if an error occurred.<br>
     *         List is empty if no actor page matching the name was found.<br>
     *         Otherwise List contains {@link JsonObject}s of the meta data of the matching pages.
     */
    public static Optional<List<JsonObject>> getActorWikiPagesByName(String name) {
        Optional<String> opt = retrieveJsonByName(name);
        return opt.map(json -> collectPages(name, json, MediaWiki::isActor));
    }

    /**
     * Returns a list of possible film Wiki pages matching the given name, sorted by similarity
     * where the first element has the greatest similarity to the given name.
     * The similarity is measured by {@link de.util.StringUtils#normedMinimumEditDistance(String, String)}
     * If an error occurred during data retrieval the Optional is empty, if only non-film pages
     * match the given name, the list is empty.
     * To retrieve the film data from a page see {@link MediaWiki#getFilmDataFromWikiPage}
     *
     * @param name partial or complete name of the desired film
     * @return Optional is empty if an error occurred.<br>
     *         List is empty if no film page matching the name was found.<br>
     *         Otherwise List contains {@link JsonObject}s of the meta data of the matching pages.
     */
    public static Optional<List<JsonObject>> getFilmWikiPagesByName(String name) {
        Optional<String> opt = retrieveJsonByName(name);
        return opt.map(json -> collectPages(name, json, MediaWiki::isFilm));
    }

    private static Optional<String> retrieveJsonByName(String name) {
        String url = createQueryURL(wikipedia.getName(), name);
        return retrieveJsonFromURL(url);
    }

    private static Optional<String> retrieveJsonFromURL(String url) {
        try {
            return Optional.of(wikipedia.getTextFile(url));
        } catch (IOException ioe) {
            return Optional.empty();
        }
    }

    private static boolean isActor(JsonObject page) {
        Predicate<String> actorCriterion = s -> s.contains("actors") || s.contains("actress");
        return isPageOfCategory(page, actorCriterion);
    }

    /**
     * Checks if the given page meta data matches a title criterion
     *
     * @param page meta data of the page
     * @param criterion criterion for the page title
     * @return <i>true</i> if and only if matching criterion<br>
     *         <i>false</i> if not matching criterion or the page meta data are incompatible
     */
    static boolean isPageOfCategory(JsonObject page, Predicate<String> criterion) {
        JsonArray categories = page.getAsJsonArray("categories");
        if(categories == null)
            return false;
        return range(0, categories.size())
                .mapToObj(categories::get)
                .map(JsonElement::getAsJsonObject)
                .filter(Objects::nonNull)
                .map(o -> o.get("title").getAsString())
                .anyMatch(criterion);

    }

    private static boolean isFilm(JsonObject page) {
        Predicate<String> filmCriterion = s -> s.matches("Category:\\d{4} films");
        return isPageOfCategory(page, filmCriterion);
    }

    private static List<JsonObject> collectPages(String name,
                                                 String jsonString,
                                                 Predicate<JsonObject> criterion) {
        try {
            final JsonObject result = JsonParser.parseString(jsonString).getAsJsonObject();
            final JsonObject query = result.getAsJsonObject("query");
            final JsonObject pages = query.getAsJsonObject("pages");

            final ToDoubleFunction<JsonObject> keyExtractor = p -> comparePageToName(p, name);

            return stream(pages.keySet().spliterator(), false)
                    .map(pages::getAsJsonObject)
                    .filter(criterion)
                    .sorted(comparing(keyExtractor::applyAsDouble))
                    .collect(toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static double comparePageToName(JsonObject page, String name) {
        String title = page.get("title").getAsString();
        double sim = normedMinimumEditDistance(title, name);
        return 1.0 - sim;
    }

    /**
     * Loads the html document from the given meta data of the page and extracts the film data
     * from this document. If something fails during data retrieval or parsing an empty Optional
     * will be returned, otherwise the optional contains the film data.
     *
     * @param page metadata of the film page
     * @return returns Optional of the film data, empty if something went wrong
     */
    public static Optional<Film> getFilmDataFromWikiPage(JsonObject page) {
        return getDataFromWikiPage(page, FILM_LOADER::loadDataFromWikiHTML);
    }

    /**
     * Loads the html document from the given meta data of the page and extracts the actor data
     * from this document. If something fails during data retrieval or parsing an empty Optional
     * will be returned, otherwise the optional contains the actor data.
     *
     * @param page metadata of the actor page
     * @return returns Optional of the actor data, empty if something went wrong
     */
    public static Optional<Actor> getActorDataFromWikiPage(JsonObject page) {
        return getDataFromWikiPage(page, ACTOR_LOADER::loadDataFromWikiHTML);
    }

    private static <P> Optional<P> getDataFromWikiPage(JsonObject page,
                                                       Function<Document, Optional<P>> load) {
        try {
            String url = page.get("fullurl").getAsString();
            Document doc = wikipedia.getHTMLDocument(url);
            return load.apply(doc);
        } catch (IOException ioe) {
            return Optional.empty();
        }
    }

    public static String asAbsolutePath(String url) {
        return wikipedia.asAbsolutePath(url);
    }
}


