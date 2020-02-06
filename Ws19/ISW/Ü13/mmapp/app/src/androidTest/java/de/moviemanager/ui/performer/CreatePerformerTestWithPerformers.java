package de.moviemanager.ui.performer;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.GrantPermissionRule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.moviemanager.R;
import de.moviemanager.data.Movie;
import de.moviemanager.data.MovieTransformations;
import de.moviemanager.data.PerformerTransformations;
import de.moviemanager.ui.MasterActivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.selectItemFromMoviePerformerSelectionDialog;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class CreatePerformerTestWithPerformers {

    @Rule
    public ActivityScenarioRule<MasterActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MasterActivity.class);
    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE = grant(WRITE_EXTERNAL_STORAGE);

    public static void populateStorageWithMoviesAndPerformers() {
        Movie starWars = STORAGE.newMovie()
                .addOperation(MovieTransformations.setTitle("Star Wars"))
                .commit()
                .get();
        Movie red = STORAGE.newMovie()
                .addOperation(MovieTransformations.setTitle("R.E.D."))
                .commit()
                .get();
        Movie pulpFiction = STORAGE.newMovie()
                .addOperation(MovieTransformations.setTitle("Pulp Fiction"))
                .commit()
                .get();

        STORAGE.newPerformer(starWars)
                .addOperation(PerformerTransformations.setName("Harrison Ford")).commit();
        STORAGE.newPerformer(pulpFiction)
                .addOperation(PerformerTransformations.setName("Samuel Leroy Jackson")).commit();
        STORAGE.newPerformer(red)
                .addOperation(PerformerTransformations.setName("Karl Urban")).commit();
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Before
    public void prepareTests() {
        populateStorageWithMoviesAndPerformers();
    }

    @Test
    public void testCreatePerformerWithExistingPerformersAndExistingMovies() {
        String performerName = "Bruce Willis";

        changeToPerformerView();
        clickAddButton();
        selectItemFromMoviePerformerSelectionDialog(1);
        onView(withId(R.id.positive_button)).perform(click());
        onView(withId(R.id.performer_name_input))
                .perform(typeText(performerName), closeSoftKeyboard());
        onView(withId(R.id.linked_movies_list)).check(matches(hasDescendant(withText("R.E.D."))));
        onView(withId(R.id.commit)).perform(click());
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText(performerName))));
    }

    public void changeToPerformerView() {
        onView(withId(R.id.bottom_navigation_performers)).perform(click());
    }

    public void clickAddButton() {
        onView(withId(R.id.add_button)).perform(click());
    }
}
