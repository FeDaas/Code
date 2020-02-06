package de.moviemanager.ui.movie;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.GrantPermissionRule;

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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.rule.GrantPermissionRule.grant;
import static de.moviemanager.data.MovieTransformations.setTitle;
import static de.moviemanager.ui.util.UiTestUtils.STORAGE;
import static de.moviemanager.ui.util.UiTestUtils.checkPermissionState;
import static de.moviemanager.ui.util.UiTestUtils.clearStorage;
import static de.moviemanager.ui.util.UiTestUtils.selectMenuItemAndEnterEdit;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ChangeMovieDataTest {
    @Rule
    public ActivityScenarioRule<MasterActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MasterActivity.class);
    @ClassRule
    public static final GrantPermissionRule PERMISSION_RULE = grant(WRITE_EXTERNAL_STORAGE);

    @BeforeClass
    public static void initStorage() {
        final ActivityScenario<MasterActivity> activityScenario = launch(MasterActivity.class);
        activityScenario.onActivity(activity -> {
            grant(WRITE_EXTERNAL_STORAGE);
            checkPermissionState(activity);
            clearStorage();
            populateStorage();
        });
    }

    private static void populateStorage() {
        STORAGE.newMovie().addOperation(setTitle("Blade Runner")).commit();
    }

    @Test
    public void testChangeMovieDetailDataWithoutConfirmation() {
        selectMenuItemAndEnterEdit("Blade Runner");

        onView(withId(R.id.movie_title_input))
                .perform(clearText(),
                        typeText("Star Wars"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.edit_runtime))
                .perform(clearText(),
                        typeText("180"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.edit_description))
                .perform(typeText("Hello There"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.edit_languages))
                .perform(scrollTo(),
                        typeText("English"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.edit_production_locations))
                .perform(scrollTo(),
                        typeText("USA"),
                        closeSoftKeyboard()
                );
        onView(withId(R.id.edit_filming_locations))
                .perform(scrollTo(),
                        typeText("in some shed"),
                        closeSoftKeyboard()
                );

        onView(withId(R.id.add_release_button)).perform(scrollTo(), click());
        onView(withId(R.id.edit_release_name_input))
                .inRoot(isDialog())
                .perform(typeText("Great Britain"),
                        closeSoftKeyboard()
                );
        // FIXME NullPointerException
        /*
        selectNumberPickerValue(
                R.id.edit_release_day,
                22,
                getCurrentActivityInstance()
        );
        selectNumberPickerValue(
                R.id.edit_release_month,
                9,
                getCurrentActivityInstance()
        );
        selectNumberPickerValue(
                R.id.edit_release_year,
                1998,
                getCurrentActivityInstance()
        );
         */
        onView(withText("CONFIRM")).perform(click());
    }
}
