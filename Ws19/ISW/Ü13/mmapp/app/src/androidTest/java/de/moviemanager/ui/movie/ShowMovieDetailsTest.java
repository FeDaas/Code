package de.moviemanager.ui.movie;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.GrantPermissionRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.moviemanager.R;
import de.moviemanager.data.Movie;
import de.moviemanager.data.PerformerTransformations;
import de.moviemanager.ui.MasterActivity;
import de.moviemanager.util.AndroidStringUtils;
import de.util.Pair;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.data.MovieTransformations.setDescription;
import static de.moviemanager.data.MovieTransformations.setFilmingLocations;
import static de.moviemanager.data.MovieTransformations.setLanguages;
import static de.moviemanager.data.MovieTransformations.setProductionLocations;
import static de.moviemanager.data.MovieTransformations.setRating;
import static de.moviemanager.data.MovieTransformations.setReleases;
import static de.moviemanager.data.MovieTransformations.setRuntime;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.data.MovieTransformations.setWatchDate;
import static de.moviemanager.ui.util.UiTestUtils.DF;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.checkPermissionState;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.isVisible;
import static de.moviemanager.ui.util.UiTestUtils.withDate;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ShowMovieDetailsTest {

    private static final String TAG = "SMT";

    @Rule
    public ActivityScenarioRule<MasterActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MasterActivity.class);
    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE = grant(WRITE_EXTERNAL_STORAGE);

    @BeforeClass
    public static void initStorage() {
        ActivityScenario<MasterActivity> activityScenario = launch(MasterActivity.class);
        activityScenario.onActivity(activity -> {
            checkPermissionState(activity);
            clearStorage();
            try {
                populateStorage();
            } catch (ParseException e) {
                Log.println(1, TAG, "The date could not be parsed.");
            }
        });
    }

    private static void populateStorage() throws ParseException {
        final String movieTitle = "Star Wars";
        final String movieDescription = "Star Wars description.";
        final String countryOfOrigin = "USA";
        final List<String> languages = Arrays.asList("English", "French");
        final List<Pair<String, Date>> releases = singletonList(Pair.paired("Germany", DF.parse("09.02.1978")));
        final String watchDate = "09.03.2016";
        final String performerName = "Harrison Ford";
        int runtime = 180;
        double movieRating = 4.5;
        double performerRating = 5;

        final Movie starWars = STORAGE.newMovie()
                .addOperation(setTitle(movieTitle))
                .addOperation(setRating(movieRating))
                .addOperation(setDescription(movieDescription))
                .addOperation(setFilmingLocations(singletonList(countryOfOrigin)))
                .addOperation(setLanguages(languages))
                .addOperation(setReleases(releases))
                .addOperation(setProductionLocations(singletonList(countryOfOrigin)))
                .addOperation(setRuntime(runtime))
                .addOperation(setWatchDate(DF.parse(watchDate)))
                .commit().get();

        STORAGE.newPerformer(starWars)
                .addOperation(PerformerTransformations.setName(performerName))
                .addOperation(PerformerTransformations.setRating(performerRating))
                .commit().get();
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testShowMovieDetails() {
        Movie starWars = STORAGE.getMovieById(0).get();
        onView(withId(R.id.portrayables))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(starWars.getTitle())), click()
                ));

        onView(withText(starWars.getTitle())).check(matches(isDisplayed()));
        onView(allOf(
                withId(R.id.watch_date),
                withDate(starWars.getWatchDate())
        )).check(matches(isDisplayed()));
        onView(withId(R.id.description)).check(matches(withText(starWars.getDescription())));
        onView(withId(R.id.rating)).check(matches(isDisplayed()));
        onView(withId(R.id.overall_rating)).check(matches(isDisplayed()));
        onView(allOf(
                withId(R.id.languages),
                withText(AndroidStringUtils.join("\n", starWars.getLanguages()))
        )).check(matches(isVisible()));
        onView(allOf(
                withId(R.id.releases),
                hasDescendant(allOf(
                        withText(starWars.getReleases().get(0).first),
                        withText(DF.format(starWars.getReleases().get(0).second))
                ))
        ));
        onView(allOf(
                withId(R.id.filming_locations),
                withText(starWars.getFilmingLocations().get(0))
        )).check(matches(isVisible()));
        onView(allOf(
                withId(R.id.runtime),
                withText(Integer.toString(starWars.getRuntime()))
        )).check(matches(isVisible()));
    }
}
