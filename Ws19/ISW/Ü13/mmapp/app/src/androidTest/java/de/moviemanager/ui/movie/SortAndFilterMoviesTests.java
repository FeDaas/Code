package de.moviemanager.ui.movie;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
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

import de.moviemanager.R;
import de.moviemanager.data.Movie;
import de.moviemanager.ui.MasterActivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.data.MovieTransformations.setRating;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.atPosition;
import static de.moviemanager.ui.util.UiTestUtils.checkPermissionState;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class SortAndFilterMoviesTests {

    private static final String TAG = "SFT";
    @Rule
    public ActivityScenarioRule<MasterActivity> activityScenarioRule = new ActivityScenarioRule<>(MasterActivity.class);
    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE = grant(WRITE_EXTERNAL_STORAGE);

    private final Movie starWars = STORAGE.getMovieById(0).get();
    private final Movie red = STORAGE.getMovieById(1).get();
    private final Movie pulpFiction = STORAGE.getMovieById(2).get();

    @BeforeClass
    public static void initStorage() {
        ActivityScenario<MasterActivity> activityScenario = launch(MasterActivity.class);
        activityScenario.onActivity(activity -> {
            checkPermissionState(activity);
            try {
                populateStorageWithMovies();
            } catch (ParseException e) {
                Log.println(1, TAG, "Date parsing failed!");
            }
        });
    }

    private static void populateStorageWithMovies() throws ParseException {
        STORAGE.newMovie()
                .addOperation(setTitle("Star Wars"))
                .addOperation(setRating(4.5))
                .commit();
        STORAGE.newMovie()
                .addOperation(setTitle("R.E.D."))
                .addOperation(setRating(4.0))
                .commit();
        STORAGE.newMovie()
                .addOperation(setTitle("Pulp Fiction"))
                .addOperation(setRating(5.0))
                .commit();
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testSortMoviesByTitleDescending() {
        clickCriterionButton();
        onView(withText("Title")).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        /*
                            Separators and the padding between items are views too.
                            The sequence is (separator), item, (padding).
                            Brackets mean, that this item is optional.
                            A separator will be added if the item following is the first in a group
                            e.g. the first item with the first letter B.
                            Padding will be added at the end of a group.
                         */
                        atPosition(1, hasDescendant(withText(starWars.getTitle()))),
                        atPosition(4, hasDescendant(withText(red.getTitle()))),
                        atPosition(7, hasDescendant(withText(pulpFiction.getTitle())))
                )
        ));
    }

    @Test
    public void testSortMoviesByRatingAscending() {
        selectCriterionAndDirection("Rating", DIRECTION.ASC.value);
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        atPosition(5, hasDescendant(withText(pulpFiction.getTitle()))),
                        atPosition(2, hasDescendant(withText(starWars.getTitle()))),
                        atPosition(1, hasDescendant(withText(red.getTitle())))
                )
        ));
    }

    @Test
    public void testSortMoviesByRatingDescending() {
        selectCriterionAndDirection("Rating", DIRECTION.DSC.value);
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        atPosition(1, hasDescendant(withText(pulpFiction.getTitle()))),
                        atPosition(4, hasDescendant(withText(starWars.getTitle()))),
                        atPosition(5, hasDescendant(withText(red.getTitle())))
                )
        ));
    }

    @Test
    public void testSortMoviesByOverallRatingAscending() {
        selectCriterionAndDirection("Overall Rating", DIRECTION.ASC.value);
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        atPosition(5, hasDescendant(withText(pulpFiction.getTitle()))),
                        atPosition(2, hasDescendant(withText(starWars.getTitle()))),
                        atPosition(1, hasDescendant(withText(red.getTitle())))
                )
        ));
    }

    @Test
    public void testSortMoviesByOverallRatingDescending() {
        selectCriterionAndDirection("Overall Rating", DIRECTION.DSC.value);
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        atPosition(1, hasDescendant(withText(pulpFiction.getTitle()))),
                        atPosition(4, hasDescendant(withText(starWars.getTitle()))),
                        atPosition(5, hasDescendant(withText(red.getTitle())))
                )
        ));
    }

    @Test
    public void testFilterMoviesByTitle() {
        selectCriterionAndDirection("Title", DIRECTION.ASC.value);
        onView(withId(R.id.filter)).perform(typeText(starWars.getTitle()));
        onView(withId(R.id.portrayables)).check(matches(
                allOf(
                        not(hasDescendant(withText(pulpFiction.getTitle()))),
                        not(hasDescendant(withText(red.getTitle())))
                )
        ));
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText(starWars.getTitle()))));
    }

    private void clickCriterionButton() {
        onView(withId(R.id.criterion_button)).perform(click());
    }

    /**
     * @param criterion The criterion that should be used.
     * @param direction The sorting direction that should be used. 1 is ascending, 2 descending
     */
    private void selectCriterionAndDirection(String criterion, int direction) {
        if (criterion.equals("Title") || criterion.equals("First Name")) {
            direction--;
        }

        while (direction > 0) {
            clickCriterionButton();
            onView(withText(criterion)).inRoot(isPlatformPopup()).perform(click());
            --direction;
        }
    }

    private enum DIRECTION {
        ASC(1), DSC(2);

        private final int value;

        DIRECTION(int value) {
            this.value = value;
        }
    }
}
