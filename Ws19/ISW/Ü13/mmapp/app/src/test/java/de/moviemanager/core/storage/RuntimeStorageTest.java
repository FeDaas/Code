package de.moviemanager.core.storage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import de.moviemanager.core.json.JsonBridge;
import de.moviemanager.core.json.MovieFromJsonObject;
import de.moviemanager.core.json.PerformerFromJsonObject;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.PerformerTransformations;
import de.storage.StorageException;
import de.util.DateUtils;
import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;
import de.util.operationflow.ReversibleTransaction;
import de.util.operationflow.Transaction;

import static de.moviemanager.core.storage.AssociationsGroup.loadMappings;
import static de.moviemanager.data.MovieTransformations.setDescription;
import static de.moviemanager.data.MovieTransformations.setFilmingLocations;
import static de.moviemanager.data.MovieTransformations.setLanguages;
import static de.moviemanager.data.MovieTransformations.setRating;
import static de.moviemanager.data.MovieTransformations.setReleases;
import static de.moviemanager.data.MovieTransformations.setRuntime;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.data.MovieTransformations.setWatchDate;
import static de.moviemanager.data.PerformerTransformations.setName;
import static de.util.Pair.paired;
import static de.util.operationflow.ReversibleOperations.reversibleTransformation;
import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeStorageTest {
    private static final Path HOME = Paths.get("TestRuntimeStorage");
    private static final String TEST_TITLE = "Test Title";
    private static final int TEST_RUNTIME = -131;

    private static final String TEST_NAME = "Test Name";
    private static final String TEST_BIRTH_NAME = "Test Birth Name";

    private RuntimeStorage storage;

    private static final Supplier<StorageException> MOVIE_CREATION_FAILED = () -> new StorageException("Creation of Movie failed");
    private static final Supplier<StorageException> PERFORMER_CREATION_FAILED = () -> new StorageException("Creation of Performer failed");

    @BeforeEach
    void init() {
        storage = RuntimeStorage.getInstance(HOME.toFile());
        storage.clear();
    }

    @AfterEach
    void tearDown() {
        storage.selfDestruct();
    }

    @Test
    void testMovieFileCreation() {
        // setup
        final Transaction<Movie, ReversibleTransformation<Movie>> transaction = storage.newMovie()
                .addOperation(reversibleTransformation(
                        Movie::getTitle,
                        Movie::setTitle,
                        TEST_TITLE)
                )
                .addOperation(reversibleTransformation(
                        Movie::getRuntime,
                        Movie::setRuntime,
                        TEST_RUNTIME)
                );
        final Path moviePath = HOME.resolve("movies").resolve("movie_0.json");

        // precondition
        assertFalse(moviePath.toFile().exists());

        // test
        final Optional<Movie> movieOpt = transaction.commit();
        assertTrue(moviePath.toFile().exists());
        assertTrue(movieOpt.isPresent());

        final Movie movie = movieOpt.get();
        assertEquals(0, movie.id());
        assertEquals(TEST_TITLE, movie.getTitle());
        assertEquals(TEST_RUNTIME, movie.getRuntime());
    }

    @Test
    void testPerformerFileCreation() {
        // setup
        final Optional<Movie> movieOpt = storage.newMovie()
                .addOperation(reversibleTransformation(
                        Movie::getTitle,
                        Movie::setTitle,
                        TEST_TITLE)
                )
                .addOperation(reversibleTransformation(
                        Movie::getRuntime,
                        Movie::setRuntime,
                        TEST_RUNTIME)
                ).commit();
        final Movie movie = movieOpt.orElse(null);
        final Path performerPath = HOME.resolve("performers").resolve("performer_0.json");

        // precondition
        assertTrue(movieOpt.isPresent());
        assertFalse(performerPath.toFile().exists());

        // test
        final Transaction<Performer, ReversibleTransformation<Performer>> pTransaction = storage.newPerformer(movie)
                .addOperation(PerformerTransformations.setName(TEST_NAME))
                .addOperation(PerformerTransformations.setBirthName(TEST_BIRTH_NAME));

        final Optional<Performer> performerOpt = pTransaction.commit();
        assertTrue(performerPath.toFile().exists());
        assertTrue(performerOpt.isPresent());

        final Performer performer = performerOpt.get();
        assertEquals(0, performer.id());
        assertEquals(TEST_NAME, performer.getName());
        assertEquals(TEST_BIRTH_NAME, performer.getBirthName());
    }

    @Test
    void testMoviePerformerFileLinkWithRollback() throws Throwable {
        // setup
        final Movie movie = storage.newMovie()
                .addOperation(reversibleTransformation(
                        Movie::getTitle,
                        Movie::setTitle,
                        TEST_TITLE))
                .addOperation(reversibleTransformation(
                        Movie::getRuntime,
                        Movie::setRuntime,
                        TEST_RUNTIME))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final ReversibleTransaction<Performer> pTransaction = storage.newPerformer(movie);
        final Performer performer = pTransaction
                .addOperation(PerformerTransformations.setName(TEST_NAME))
                .addOperation(PerformerTransformations.setBirthName(TEST_BIRTH_NAME))
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);

        final Path performerPath = HOME.resolve("performers").resolve("performer_0.json");

        // test
        pTransaction.rollback();
        assertFalse(performerPath.toFile().exists());
        assertFalse(storage.isLinked(movie, performer));
    }

    @Test
    void testWrittenMovieFilesEquality() throws Throwable {
        // setup
        final Date d = DateUtils.now();
        final Movie m1 = storage.newMovie()
                .addOperation(setTitle("The Movie"))
                .addOperation(setLanguages(asList("English", "German")))
                .addOperation(setReleases(asList(paired("Loc1", d), paired("Loc2", d))))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m3 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);

        // precondition
        assertEquals(0, m1.id());
        assertEquals(1, m2.id());
        assertEquals(2, m3.id());

        // test
        final Movie m1R = readMovieFromFile("movie_0.json");
        final Movie m2R = readMovieFromFile("movie_1.json");
        final Movie m3R = readMovieFromFile("movie_2.json");

        assertEquals(m1, m1R);
        assertEquals(m2, m2R);
        assertEquals(m3, m3R);
    }

    private Movie readMovieFromFile(final String fName) throws Exception {
        final Path path = HOME.resolve("movies").resolve(fName);
        final String jsonString = String.join("", readAllLines(path));
        final JSONObject json = new JSONObject(jsonString);
        return JsonBridge.fromJson(json, MovieFromJsonObject::new).orElse(null);
    }

    @Test
    void testWrittenPerformerFilesEquality() throws Throwable {
        // setup
        final Date d = DateUtils.now();
        final Movie m1 = storage.newMovie()
                .addOperation(setTitle("The Movie"))
                .addOperation(setLanguages(asList("English", "German")))
                .addOperation(setReleases(asList(paired("Loc1", d), paired("Loc2", d))))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m3 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);

        final Performer p1 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer p2 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer p3 = storage.newPerformer(m3)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);

        storage.link(m2, p1);
        storage.link(m3, p1);
        storage.link(m2, p3);

        // precondition
        assertEquals(0, p1.id());
        assertEquals(1, p2.id());
        assertEquals(2, p3.id());

        // test
        Performer p1R = readPerformerFromFile("performer_0.json");
        Performer p2R = readPerformerFromFile("performer_1.json");
        Performer p3R = readPerformerFromFile("performer_2.json");

        assertEquals(p1, p1R);
        assertEquals(p2, p2R);
        assertEquals(p3, p3R);
    }

    private Performer readPerformerFromFile(String fName) throws Exception {
        final Path path = HOME.resolve("performers").resolve(fName);
        final String jsonString = String.join("", readAllLines(path));
        final JSONObject json = new JSONObject(jsonString);
        return JsonBridge.fromJson(json, PerformerFromJsonObject::new).orElse(null);
    }

    @Test
    void testWrittenAssociations() throws Throwable {
        // setup
        final Path path = HOME.resolve("associations").resolve("Movie-Performer.json");
        final List<String> lines = Files.readAllLines(path);
        final List<Pair<Integer, Integer>> mapping = loadMappings(lines);

        final Movie m1 = storage.newMovie()
                .addOperation(setTitle("Movie 1"))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .addOperation(setTitle("Movie 2"))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);

        final Performer p1 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer p2 = storage.newPerformer(m2)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);

        // precondition
        assertTrue(path.toFile().exists());
        assertTrue(mapping.isEmpty());

        // test
        compareIdPairs(path, asList(paired(0, 0), paired(1, 1)));

        storage.link(m2, p1);
        storage.unlink(m2, p2);

        compareIdPairs(path, asList(paired(0, 0), paired(1, 0)));

        storage.removeMovie(m1).commit();
        compareIdPairs(path, singletonList(paired(1, 0)));
    }

    private void compareIdPairs(final Path path,
                                final List<Pair<Integer, Integer>> expected)
            throws IOException, JSONException {
        assertTrue(path.toFile().exists());
        final List<String> lines = Files.readAllLines(path);
        final List<Pair<Integer, Integer>> mapping = loadMappings(lines).stream()
                .sorted(comparing(Pair::getFirst))
                .collect(toList());
        assertFalse(mapping.isEmpty());

        final List<Pair<Integer, Integer>> expectedMapping = expected
                .stream()
                .sorted(comparing(Pair::getFirst))
                .collect(toList());
        assertEquals(expectedMapping, mapping);
    }

    @Test
    void testRemoveMovieTransaction() throws Throwable {
        // setup
        final Movie m1 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);

        final Performer p1 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer p2 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer p3 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        storage.link(m2, p3);

        // precondtion
        assertTrue(storage.isLinked(m1, p1));
        assertTrue(storage.isLinked(m1, p2));
        assertTrue(storage.isLinked(m1, p3));
        assertTrue(storage.isLinked(m2, p3));
        assertFalse(storage.getPerformers().isEmpty());

        // test
        Transaction<Movie, ?> removal = storage.removeMovie(m1);
        removal.commit();
        assertEquals(1, storage.getPerformers().size());
        assertTrue(storage.getPerformers().contains(p3));
        assertEquals(1, storage.getMovies().size());
        assertTrue(storage.getMovies().contains(m2));

        removal.rollback();
        assertEquals(3, storage.getPerformers().size());
        assertFalse(storage.getPerformers().isEmpty());
        assertTrue(storage.isLinked(m1, p1));
        assertTrue(storage.isLinked(m1, p2));
        assertTrue(storage.isLinked(m1, p3));
        assertTrue(storage.isLinked(m2, p3));
    }

    @Test
    void testRemovePerformerTransaction() throws Throwable {
        // setup
        final Movie m1 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);

        final Performer p1 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        storage.link(m2, p1);

        // precondition
        assertTrue(storage.isLinked(m1, p1));
        assertTrue(storage.isLinked(m2, p1));
        assertFalse(storage.getPerformers().isEmpty());

        // test
        final Transaction<Performer, ?> removal = storage.removePerformer(p1);
        removal.commit();
        assertTrue(storage.getPerformers().isEmpty());
        assertTrue(storage.getLinkedMoviesOfPerformer(p1).isEmpty());

        removal.rollback();
        assertEquals(2, storage.getLinkedMoviesOfPerformer(p1).size());
        assertFalse(storage.getPerformers().isEmpty());
    }

    @Test
    void testCreateMovieWithAllAttributesWithRollback() {
        // setup
        final Transaction<Movie, ReversibleTransformation<Movie>> transaction = storage.newMovie()
                .addOperation(setTitle("Test Movie"))
                .addOperation(setLanguages(singletonList("English")))
                .addOperation(setWatchDate(new Date(0L)))
                .addOperation(setDescription("Lorem ipsum dolor sit amet"))
                .addOperation(setReleases(singletonList(paired("unk", new Date()))))
                .addOperation(setFilmingLocations(singletonList("USA")))
                .addOperation(setRuntime(1021));

        // precondition
        assertTrue(storage.getMovies().isEmpty());

        // test
        final Optional<Movie> movieOpt = transaction.commit();
        assertTrue(movieOpt.isPresent());
        final Movie movie = movieOpt.get();
        assertEquals(1, storage.getMovies().size());
        assertTrue(storage.getMovies().contains(movie));
        assertEquals(movie, storage.getMovieById(0).get());

        transaction.rollback();
        assertTrue(storage.getMovies().isEmpty());
        assertEquals(movie, new Movie(0));
        assertFalse(storage.getMovieById(0).isPresent());
    }

    @Test
    void testCreatePerformerWithAllAttributesWithRollback() throws Throwable {
        // setup
        final Movie m = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Transaction<Performer, ReversibleTransformation<Performer>> transaction = storage.newPerformer(m)
                .addOperation(setName("Name"))
                .addOperation(PerformerTransformations.setBirthName("Birth Name"))
                .addOperation(PerformerTransformations.setBiography("X"))
                .addOperation(PerformerTransformations.setDateOfBirth(new Date(0)))
                .addOperation(PerformerTransformations.setRating(100))
                .addOperation(PerformerTransformations.setOccupations(singletonList("Actor")));

        // precondition
        assertTrue(storage.getPerformers().isEmpty());

        // test
        final Optional<Performer> performerOpt = transaction.commit();
        assertTrue(performerOpt.isPresent());
        Performer performer = performerOpt.get();
        assertEquals(1, storage.getPerformers().size());
        assertTrue(storage.getPerformers().contains(performer));
        assertEquals(performer, storage.getPerformerById(0).get());

        transaction.rollback();
        assertTrue(storage.getPerformers().isEmpty());
        assertEquals(performer, new Performer(0));
        assertFalse(storage.getPerformerById(0).isPresent());
    }

    @Test
    void testMovieUpdateWithRollback() throws Throwable {
        // setup
        final Movie m = storage.newMovie().commit().orElseThrow(MOVIE_CREATION_FAILED);
        final Movie expectedRaw = new Movie(0);
        final Movie expectedUpdated = new Movie(0);
        expectedUpdated.setTitle("Updated");

        // precondition
        assertNotEquals(expectedUpdated, m);
        assertEquals(expectedRaw, m);

        // test
        final Transaction<Movie, ReversibleTransformation<Movie>> transaction = storage.updateMovie(m);
        transaction.addOperation(setTitle("Updated")).commit();
        assertEquals(expectedUpdated, m);
        assertNotEquals(expectedRaw, m);

        transaction.rollback();
        assertNotEquals(expectedUpdated, m);
        assertEquals(expectedRaw, m);
    }

    @Test
    void testPerformerUpdateWithRollback() throws Throwable {
        // setup
        final Movie m = storage.newMovie().commit().orElseThrow(MOVIE_CREATION_FAILED);
        final Performer p = storage.newPerformer(m).commit().orElseThrow(PERFORMER_CREATION_FAILED);
        final Performer expectedRaw = new Performer(0);
        final Performer expectedUpdated = new Performer(0);
        expectedUpdated.setName("Updated");

        // precondition
        assertNotEquals(expectedUpdated, p);
        assertEquals(expectedRaw, p);

        // test
        final Transaction<Performer, ReversibleTransformation<Performer>> transaction = storage.updatePerformer(p);
        transaction.addOperation(setName("Updated")).commit();
        assertEquals(expectedUpdated, p);
        assertNotEquals(expectedRaw, p);

        transaction.rollback();
        assertNotEquals(expectedUpdated, p);
        assertEquals(expectedRaw, p);
    }

    @Test
    void testMovieReload() throws Throwable {
        // setup
        final Movie m1 = storage.newMovie()
                .addOperation(setTitle("Sample Movie"))
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m1R = readMovieFromFile("movie_0.json");

        // precondition
        assertFalse(storage.getMovies().isEmpty());
        assertEquals(m1, m1R);
        assertNotSame(m1, m1R);

        // test
        storage = RuntimeStorage.getInstance(HOME.toFile());
        final Optional<Movie> m1cOpt = storage.getMovieById(0);
        assertTrue(m1cOpt.isPresent());
        final Movie m1c = m1cOpt.get();
        assertEquals(m1, m1c);
        assertSame(m1, m1c);
    }

    @Test
    void testPerformerReload() throws Throwable {
        // setup
        Movie m1 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        Performer p1 = storage.newPerformer(m1)
                .addOperation(setName("Sample Performer"))
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        Performer p1R = readPerformerFromFile("performer_0.json");

        // precondition
        assertFalse(storage.getMovies().isEmpty());
        assertEquals(p1, p1R);
        assertNotSame(p1, p1R);

        // test
        storage = RuntimeStorage.getInstance(HOME.toFile());
        final Optional<Performer> p1cOpt = storage.getPerformerById(0);
        assertTrue(p1cOpt.isPresent());
        final Performer p1C = p1cOpt.get();
        assertEquals(p1, p1C);
        assertSame(p1, p1C);
    }

    @Test
    void testAssociationsReload() throws Throwable {
        // setup
        final Path path = HOME.resolve("associations").resolve("Movie-Performer.json");
        final Movie m1 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Movie m2 = storage.newMovie()
                .commit()
                .orElseThrow(MOVIE_CREATION_FAILED);
        final Performer p1 = storage.newPerformer(m1)
                .commit()
                .orElseThrow(PERFORMER_CREATION_FAILED);
        final List<String> lines = Files.readAllLines(path);
        final List<Pair<Integer, Integer>> mapping = loadMappings(lines);

        // precondition
        assertTrue(storage.isLinked(m1, p1));
        assertFalse(storage.isLinked(m2, p1));
        assertEquals(singletonList(p1), storage.getLinkedPerformersOfMovie(m1));
        assertEquals(emptyList(), storage.getLinkedPerformersOfMovie(m2));
        assertEquals(singletonList(m1), storage.getLinkedMoviesOfPerformer(p1));
        assertEquals(singletonList(paired(0, 0)), mapping);

        // test
        storage = RuntimeStorage.getInstance(HOME.toFile());

        assertTrue(storage.isLinked(m1, p1));
        assertFalse(storage.isLinked(m2, p1));
        assertEquals(singletonList(p1), storage.getLinkedPerformersOfMovie(m1));
        assertEquals(emptyList(), storage.getLinkedPerformersOfMovie(m2));
        assertEquals(singletonList(m1), storage.getLinkedMoviesOfPerformer(p1));
    }

    @Test
    void testInstantUpdate() {
        // setup
        Optional<Movie> m1Opt = storage.newMovie()
                .addOperation(setRating(50))
                .commit();
        assertTrue(m1Opt.isPresent());
        Movie m1 = m1Opt.get();
        Transaction<Movie, ReversibleTransformation<Movie>> update = storage.updateMovie(m1)
                .addOperation(setRating(100));

        // precondition
        assertEquals(50, m1.getRating());
        assertEquals(50, storage.getMovies().get(0).getRating());

        // test
        Optional<Movie> m2Opt = update.commit();
        assertTrue(m2Opt.isPresent());
        Movie m2 = m2Opt.get();

        assertEquals(100, m2.getRating());
        assertSame(m1, m2);

        // postcondition
        assertEquals(100, m1.getRating());
        assertEquals(100, storage.getMovies().get(0).getRating());
    }
}
