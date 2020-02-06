package de.wiki;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import de.util.Pair;
import de.wiki.data.Actor;
import de.wiki.data.Film;
import de.wiki.loader.ActorLoader;
import de.wiki.query.NameBasedQuery;

import static de.wiki.MediaWiki.getActorDataFromWikiPage;
import static de.wiki.MediaWiki.getActorWikiPagesByName;
import static de.wiki.MediaWiki.getFilmDataFromWikiPage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaWikiTest {

    private final String filmPartialTitle = "lord of the rings: The Fellowship";
    private final String filmTitle = "The Lord of the Rings: The Fellowship of the Ring";

    private final String actorPartialName = "johnny d";
    private final String actorName = "Johnny Depp";

    @BeforeEach
    void init() {
        MediaWiki.resetToEnglishWikipedia();
        NameBasedQuery.setEncoding(UTF_8.name());
    }

    @Test
    void testActorPageRetrieval() throws JSONException {
        final String actorURL = "https://en.wikipedia.org/wiki/Johnny_Depp";
        testPageRetrieval(MediaWiki::getActorWikiPagesByName,
                actorPartialName,
                actorName,
                actorURL
        );
    }

    private <P> void testPageRetrieval(final Function<String, Optional<List<JsonObject>>> retrieve,
                                       final String partial,
                                       final String name,
                                       final String url) throws JSONException {
        final Optional<List<JsonObject>> pagesOpt = retrieve.apply(partial);
        assertTrue(pagesOpt.isPresent());
        final List<JsonObject> pages = pagesOpt.get();
        assertTrue(pages.size() >= 1);
        final JsonObject topPage = pages.get(0);
        assertEquals(name, topPage.get("title").getAsString());
        assertEquals(url, topPage.get("fullurl").getAsString());
    }

    @Test
    void testFilmPageRetrieval() throws JSONException {
        final String filmURL = "https://en.wikipedia.org/wiki/The_Lord_of_the_Rings:_The_Fellowship_of_the_Ring";
        testPageRetrieval(MediaWiki::getFilmWikiPagesByName,
                filmPartialTitle,
                filmTitle,
                filmURL
        );
    }

    @Test
    void testInvalidFormatForQuery() {
        NameBasedQuery.setEncoding("x");
        assertThrows(RuntimeException.class, () -> MediaWiki.getFilmWikiPagesByName(filmPartialTitle));
    }

    @Test
    void testMissingInternetConnection() {
        JsonObject actorPage = getActorWikiPagesByName(actorPartialName).get().get(0);
        JsonObject filmPage = MediaWiki.getFilmWikiPagesByName(filmPartialTitle).get().get(0);

        Wikipedia mockWiki = new NotConnectedWiki();
        MediaWiki.useOtherWiki(mockWiki);
        assertEquals(mockWiki, MediaWiki.getCurrentWikipedia());

        assertFalse(MediaWiki.getFilmWikiPagesByName(filmPartialTitle).isPresent());
        assertFalse(getActorWikiPagesByName(actorPartialName).isPresent());
        assertFalse(getFilmDataFromWikiPage(filmPage).isPresent());
        assertFalse(getActorDataFromWikiPage(actorPage).isPresent());
    }

    @Test
    void testLoadActorWithInternetConnection() {
        final JsonObject actorPage = getActorWikiPagesByName(actorPartialName)
                .get()
                .get(0);
        final Optional<Actor> actorOpt = getActorDataFromWikiPage(actorPage);
        assertTrue(actorOpt.isPresent());
        final Actor actor = actorOpt.get();

        assertEquals(actorName, actor.getName());
        assertEquals("John Christopher Depp II", actor.getBirthName());
        assertTrue(actor.getBiography().length() > 0);
        assertEquals(asList("actor", "producer", "musician"), actor.getOccupations());
        assertEquals("09 June 1963", actor.getDateOfBirth());
        assertFalse(actor.getImageURL().isEmpty());
        assertEquals("Actor{name=\"Johnny Depp\", birthday=\"09 June 1963\"," +
                " occupations=[actor, producer, musician]}", actor.toString());
    }

    @Test
    void testLoadFilmWithInternetConnection() {
        JsonObject filmPage = MediaWiki.getFilmWikiPagesByName(filmPartialTitle).get().get(0);
        Optional<Film> filmOpt = getFilmDataFromWikiPage(filmPage);
        assertTrue(filmOpt.isPresent());
        Film film = filmOpt.get();

        assertEquals(filmTitle, film.getTitle());
        assertTrue(film.getDescription().length() > 0);
        assertEquals(singletonList("English"), film.getLanguages());
        assertEquals("178 minutes", film.getRunningTime());
        assertEquals(3, film.getReleaseDates().size());
        assertEquals(Pair.paired("10 December 2001", "Odeon Leicester Square"), film.getReleaseDates().get(0));
        assertEquals(15, film.getStarring().size());
        assertEquals(asList("New Zealand", "United States"), film.getCountries());
        assertFalse(film.getImageURL().isEmpty());
        assertEquals("Film{title=\"The Lord of the Rings: The Fellowship of the Ring\", running time=\"178 minutes\", #releases=3}", film.toString());
    }

    @Test
    void testDefaultWiki() {
        final String expectedHome = "https://en.wikipedia.org";
        final String actorPath = "/wiki/Elijah_Wood";
        final Wikipedia expectedWiki = new Wikipedia("en.wikipedia.org");
        final Wikipedia actualWiki = MediaWiki.getCurrentWikipedia();

        assertEquals(expectedWiki, actualWiki);
        assertEquals(expectedWiki.hashCode(), actualWiki.hashCode());
        assertEquals(expectedWiki.toString(), actualWiki.toString());
        assertEquals(expectedHome, actualWiki.getHomeURL());
        assertEquals(expectedHome + actorPath, actualWiki.asAbsolutePath(actorPath));
    }

    @Test
    void testEmptyJSONPageForCategoryCheck() {
        JsonObject page = new JsonObject();
        assertFalse(MediaWiki.isPageOfCategory(page, String::isEmpty));
    }

    @Test
    void testNonExistingDocumentForActor() {
        ActorLoader loader = ActorLoader.getInstance();
        Optional<Actor> opt = loader.loadDataFromWikiHTML(new Document("nonExisting.html"));
        assertFalse(opt.isPresent());
    }

    @Test
    void testDataTraits() {
        Film film = new Film("film", "url", "description", new HashMap<>());
        Film film2 = new Film("film", "url", "description", new HashMap<>());
        Film film3 = new Film("sample", "url", "description", new HashMap<>());
        assertEquals(film, film2);
        assertNotEquals(film, film3);
        assertEquals(film.hashCode(), film2.hashCode());

        Actor actor = new Actor("actor", "url", "description", new HashMap<>());
        Actor actor2 = new Actor("actor", "url", "description", new HashMap<>());
        Actor actor3 = new Actor("sample", "url", "description", new HashMap<>());
        assertEquals(actor, actor2);
        assertNotEquals(actor, actor3);
        assertEquals(actor.hashCode(), actor2.hashCode());

        assertNotEquals(film3, actor3);
    }
}

class NotConnectedWiki extends Wikipedia {

    NotConnectedWiki() {
        super("no.wiki.atall");
    }

    @Override
    public Document getHTMLDocument(String url) throws IOException {
        throw new IOException("");
    }

    @Override
    public String getTextFile(String url) throws IOException {
        throw new IOException("");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NotConnectedWiki;
    }

    @Override
    public int hashCode() {
        return 0xAFFE;
    }
}
