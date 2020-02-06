package de.moviemanager.ui.movie;

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

import de.moviemanager.R;
import de.moviemanager.ui.MasterActivity;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.checkPermissionState;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.clickXY;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class RemoveMovieTestsWithoutPerformers {

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
            populateStorageWithMovies();
        });
    }

    private static void populateStorageWithMovies() {
        STORAGE.newMovie().addOperation(setTitle("Star Wars")).commit();
        STORAGE.newMovie().addOperation(setTitle("R.E.D.")).commit();
        STORAGE.newMovie().addOperation(setTitle("Pulp Fiction")).commit();
    }

    @AfterClass
    public static void clearStorageAfterClass() {
        clearStorage();
    }

    @Test
    public void testRemoveMovieWithNoLinkedPerformers() {
        onView(withId(R.id.portrayables))
                .perform(actionOnItem(
                        hasDescendant(withText("Pulp Fiction")), swipeRight()
                ));
        onView(withId(R.id.portrayables))
                .perform(actionOnItem(
                        hasDescendant(withText("Pulp Fiction")), clickXY(-26, 0)
                ));

        onView(withId(R.id.portrayables)).check(matches(not(hasDescendant(withText("Pulp Fiction")))));
    }
}
