package de.moviemanager.ui.movie;

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

import de.moviemanager.R;
import de.moviemanager.data.Movie;
import de.moviemanager.ui.MasterActivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.data.PerformerTransformations.setName;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.checkPermissionState;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.clickXY;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class RemoveMovieTestsWithPerformers {

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
            populateStorageWithMoviesAndPerformers();
        });
    }

    private static void populateStorageWithMoviesAndPerformers() {
        Movie starWars = STORAGE.newMovie()
                .addOperation(setTitle("Star Wars"))
                .commit().get();
        Movie red = STORAGE.newMovie()
                .addOperation(setTitle("R.E.D."))
                .commit().get();
        Movie pulpFiction = STORAGE.newMovie()
                .addOperation(setTitle("Pulp Fiction"))
                .commit().get();

        STORAGE.newPerformer(starWars)
                .addOperation(setName("Harrison Ford"))
                .commit();
        STORAGE.newPerformer(pulpFiction)
                .addOperation(setName("Samuel Leroy Jackson"))
                .commit();
        STORAGE.newPerformer(red)
                .addOperation(setName("Karl Urban"))
                .commit();

        STORAGE.link(STORAGE.getMovieById(0).get(), STORAGE.getPerformerById(1).get());
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testRemoveMovieNoConfirmation() {
        onView(withId(R.id.portrayables))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Star Wars")), swipeRight()
                ));
        onView(withId(R.id.portrayables))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Star Wars")), click()
                ));

        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText("Star Wars"))));
    }

    @Test
    public void testRemoveMovieUndoWithPerformerOnlyLinkedToTheMovie() {
        selectAndConfirmDeletion("Star Wars");
        onView(withText("YES")).perform(click());
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()));
        onView(withText("UNDO")).perform(click());
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText("Star Wars"))));
        onView(withId(R.id.bottom_navigation_performers)).perform(click());
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText("Samuel Leroy Jackson"))));
    }

    @Test
    public void testRemoveMovieWithPerformerOnlyLinkedToTheMovieWithCancellation() {
        selectAndConfirmDeletion("Star Wars");
        onView(withText("NO")).perform(click());
        onView(withId(R.id.portrayables)).check(matches(hasDescendant(withText("Star Wars"))));
    }

    @Test
    public void testRemoveMovieWithPerformerOnlyLinkedToTheMovieNoCancellation() {
        selectAndConfirmDeletion("R.E.D.");
        onView(withText("YES")).perform(click());
        onView(withId(R.id.portrayables)).check(matches(not(hasDescendant(withText("R.E.D.")))));
    }

    @Test
    public void testRemoveMovieWithoutPerformerOnlyLinkedToTheMovie() {
        selectAndConfirmDeletion("Pulp Fiction");
        onView(withId(R.id.portrayables)).check(matches(not(hasDescendant(withText("Pulp Fiction")))));
    }

    private void selectAndConfirmDeletion(String itemName) {
        onView(withId(R.id.portrayables))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(itemName)), swipeRight()
                ));
        onView(withId(R.id.portrayables))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(itemName)), clickXY(-26, 0)
                ));
    }
}
