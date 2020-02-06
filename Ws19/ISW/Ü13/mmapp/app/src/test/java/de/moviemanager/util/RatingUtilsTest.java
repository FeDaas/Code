package de.moviemanager.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.moviemanager.core.storage.RuntimeStorage;
import de.moviemanager.core.storage.RuntimeStorageConcept;
import de.moviemanager.data.Movie;
import de.moviemanager.data.MovieTransformations;
import de.moviemanager.data.Performer;
import de.moviemanager.data.PerformerTransformations;
import de.util.Pair;

import static de.moviemanager.util.RatingUtils.calculateOverallRating;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingUtilsTest {
    private final Random random = new Random();
    private RuntimeStorageConcept storage;

    @BeforeEach
    void setup() {
        storage = RuntimeStorage.getInstance(new File("TempTestStorage_" + System.currentTimeMillis()));
        RatingUtils.mockStorage(storage);
    }

    @AfterEach
    void tearDown() {
        RatingUtils.removeMock();
        storage.selfDestruct();
    }

    @Test
    @DisplayName("Overall Rating - unrated Movie - no Performers")
    void testCalculateOverallRatingWithUnratedMovieAndNoPerformers() {
        final Movie movie = new Movie(0);
        final List<Performer> performers = createPerformersFromRating();

        // precondition
        assertTrue(movie.isUnrated());
        assertTrue(performers.isEmpty());

        // test
        assertEquals(-1.f, calculateOverallRating(movie, performers));

        // postcondition
        assertTrue(movie.isUnrated());
    }

    private static List<Performer> createPerformersFromRating(double... ratings) {
        List<Performer> result;
        if (ratings.length == 0) {
            result = Collections.emptyList();
        } else {
            result = IntStream.range(0, ratings.length)
                    .mapToObj(i -> Pair.paired(i, ratings[i]))
                    .map(RatingUtilsTest::createRatedPerformer)
                    .collect(Collectors.toList());
        }

        return result;
    }

    private static Performer createRatedPerformer(final Pair<Integer, Double> data) {
        final Performer p = new Performer(data.first);
        p.setRating(data.second);
        return p;
    }

    @Test
    @DisplayName("Overall Rating - rated Movie - multiple unrated Performers")
    void testCalculateOverallRatingWithRatedMovieAndMultipleUnratedPerformers() {
        final Movie movie = new Movie(0);
        movie.setRating(3.5);
        final List<Performer> performers = createPerformersFromRating(-4.5, -2.5, -1.5, -5.0);

        // precondition
        assertTrue(movie.isRated());
        assertFalse(performers.isEmpty());

        // test
        assertEquals(3.5, calculateOverallRating(movie, performers));

        // postcondition
        assertTrue(movie.isRated());
    }

    @Test
    @DisplayName("Overall Rating with Storage Data")
    void testOverallRatingFromStorage() {
        // setup & preconditions
        final Optional<Movie> movieOpt = storage.newMovie().addOperation(MovieTransformations.setTitle("$Test_Movie"))
                .addOperation(MovieTransformations.setRating(3.5))
                .commit();
        assertTrue(movieOpt.isPresent());
        final Movie movie = movieOpt.get();
        final Optional<Performer> performerOpt1 = storage.newPerformer(movie)
                .addOperation(PerformerTransformations.setName("$Test_Performer_Rated"))
                .addOperation(PerformerTransformations.setRating(2.0))
                .commit();
        final Optional<Performer> performerOpt2 = storage.newPerformer(movie)
                .addOperation(PerformerTransformations.setName("$Test_Performer_Unrated"))
                .addOperation(PerformerTransformations.setRating(-2.0))
                .commit();
        assertTrue(performerOpt1.isPresent());
        assertTrue(performerOpt2.isPresent());
        final List<Performer> performers = asList(performerOpt1.get(), performerOpt2.get());

        // test
        assertEquals(2.75, calculateOverallRating(movie));
        assertEquals(calculateOverallRating(movie, performers), calculateOverallRating(movie));

        // teardown
        storage.removePerformer(performers.get(1));
        storage.removePerformer(performers.get(0));
        storage.removeMovie(movie);
        storage.selfDestruct();
    }

    @Test
    @DisplayName("\u2606\u2606\u2606\u2606\u2606 - \u2605\u2605\u2605\u2605\u2605")
    void testAllPossibilitiesForTextRatingBar() {
        final double[] ratings = {0.0, 0.5,
                1.0, 1.5,
                2.0, 2.5,
                3.0, 3.5,
                4.0, 4.5,
                5.0};
        final String[] expectedStrings = {
                "\u2606\u2606\u2606\u2606\u2606", "\u2bea\u2606\u2606\u2606\u2606",
                "\u2605\u2606\u2606\u2606\u2606", "\u2605\u2bea\u2606\u2606\u2606",
                "\u2605\u2605\u2606\u2606\u2606", "\u2605\u2605\u2bea\u2606\u2606",
                "\u2605\u2605\u2605\u2606\u2606", "\u2605\u2605\u2605\u2bea\u2606",
                "\u2605\u2605\u2605\u2605\u2606", "\u2605\u2605\u2605\u2605\u2bea",
                "\u2605\u2605\u2605\u2605\u2605"
        };
        final int maxStars = 5;

        // precondition
        assertEquals(ratings.length, expectedStrings.length);
        for (final String expectedString : expectedStrings) {
            assertEquals(maxStars, expectedString.length());
        }

        // test
        for(int i = 0; i < expectedStrings.length; ++i) {
            assertEquals(expectedStrings[i], RatingUtils.textRatingBar(ratings[i], maxStars));
        }
    }
}
