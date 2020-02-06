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
import de.moviemanager.data.MovieTransformations;
import de.moviemanager.ui.MasterActivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.selectItemFromMoviePerformerSelectionDialog;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class CreatePerformerTestsNoPerformers {

    @Rule
    public ActivityScenarioRule<MasterActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MasterActivity.class);
    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE = grant(WRITE_EXTERNAL_STORAGE);

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Before
    public void prepareTests() {
        clearStorage();
        populateStorageWithMovies();
    }

    private void populateStorageWithMovies() {
        STORAGE.newMovie().addOperation(MovieTransformations.setTitle("Star Wars")).commit();
        STORAGE.newMovie().addOperation(MovieTransformations.setTitle("R.E.D.")).commit();
        STORAGE.newMovie().addOperation(MovieTransformations.setTitle("Pulp Fiction")).commit();
    }

    @Test
    public void testCreatePerformerWithNoMoviesNoConfirmation() {
        clearStorage();
        String name = "Silvester Stallon";

        // in movie master
        changeToPerformerView();
        // in performer master
        clickAddButton();
        // in movie selection dialog
        onView(withId(R.id.negative_button)).perform(click());
        // change performer name to Silvester Stallon
        onView(withId(R.id.performer_name_input)).perform(typeText(name), closeSoftKeyboard());
        // check if confirm button is not clickable
        onView(withId(R.id.commit)).check(matches(not(isEnabled())));
        // leave the vie by pressing back
        pressBack();
        // in the warning dialog select YES
        onView(withText("Do you really want to discard changes?"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText("YES"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        // check that Silvester Stallone is not in the performer master list
        onView(withId(R.id.portrayables))
                .check(matches(not(hasDescendant(withText(name)))));
    }

    @Test
    public void testCreatePerformersWithNoPerformersAndExistingMovies() {
        String performerName = "Harrison Ford";

        changeToPerformerView();
        clickAddButton();

        // add the movie Star Wars to the linked movie list in the movie selection dialog
        selectItemFromMoviePerformerSelectionDialog(2);
        onView(withId(R.id.positive_button)).perform(click());

        onView(withId(R.id.performer_name_input))
                .perform(typeText(performerName), closeSoftKeyboard());
        // check if Star Wars is in the linked movie list
        onView(withId(R.id.linked_movies_list)).check(matches(hasDescendant(withText("Star Wars"))));
        // confirm the changes
        onView(withId(R.id.commit)).perform(click());
        // in performer master check that Harrison Ford is in the list
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText(performerName))));
    }

    public void changeToPerformerView() {
        onView(withId(R.id.bottom_navigation_performers)).perform(click());
    }

    public void clickAddButton() {
        onView(withId(R.id.add_button)).perform(click());
    }
}