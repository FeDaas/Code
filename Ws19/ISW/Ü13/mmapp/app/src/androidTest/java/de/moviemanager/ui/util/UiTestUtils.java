package de.moviemanager.ui.util;

import android.app.Activity;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import de.moviemanager.R;
import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.ui.view.DateSelectionView;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.AllOf.allOf;

public class UiTestUtils {

    public static final SimpleDateFormat DF = new SimpleDateFormat("dd.MM.yyyy");
    public static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();
    private static Activity CURRENT_ACTIVITY;

    public static void selectItemFromMoviePerformerSelectionDialog(int position) {
        onView(Matchers.allOf(withId(R.id.selection_marker),
                childAtPosition(childAtPosition(withId(R.id.portrayables), position), 0),
                isDisplayed()))
                .perform(click());
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static ViewAction clickXY(final int x, final int y) {
        return new GeneralClickAction(
                Tap.SINGLE,
                view -> {

                    final int[] screenPos = new int[2];
                    view.getLocationOnScreen(screenPos);

                    final float screenX = screenPos[0] + x;
                    final float screenY = screenPos[1] + y;
                    float[] coordinates = {screenX, screenY};

                    return coordinates;
                },
                Press.FINGER,
                InputDevice.SOURCE_TOUCHSCREEN,
                BUTTON_PRIMARY);
    }

    public static void selectMenuItemAndEnterEdit(String itemName) {
        onView(withId(R.id.portrayables)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(itemName)), click())
        );
        onView(withId(R.id.edit)).perform(click());
    }

    public static void selectNumberPickerValue(int pickerId, int targetValue, Activity currentActivity) {
        final int ROWS_PER_SWIPE = 5;
        NumberPicker numberPicker = currentActivity.findViewById(pickerId);
        ViewInteraction viewInteraction = onView(withId(pickerId));

        while (targetValue != numberPicker.getValue()) {
            int delta = Math.abs(targetValue - numberPicker.getValue());
            if (targetValue < numberPicker.getValue()) {
                if (delta >= ROWS_PER_SWIPE) {
                    viewInteraction.perform(new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER, Press.FINGER));
                } else {
                    viewInteraction.perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.TOP_CENTER, Press.FINGER));
                }
            } else {
                if (delta >= ROWS_PER_SWIPE) {
                    viewInteraction.perform(new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER));
                } else {
                    viewInteraction.perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.BOTTOM_CENTER, Press.FINGER));
                }
            }
            SystemClock.sleep(50);
        }
    }

    public static ViewAction clickOnValueInNumberPicker(int targetValue) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(
                        isDisplayed(),
                        isEnabled(),
                        isAssignableFrom(NumberPicker.class)
                );
            }

            @Override
            public String getDescription() {
                return "performing single click on item with value " + targetValue + " ";
            }

            @Override
            public void perform(UiController uiController, View numberPicker) {
                final int rowsPerSwipe = 5;

                while (targetValue != ((NumberPicker) numberPicker).getValue()) {
                    int delta = Math.abs(targetValue - ((NumberPicker) numberPicker).getValue());
                    if (targetValue < ((NumberPicker) numberPicker).getValue()) {
                        if (delta >= rowsPerSwipe) {
                            numberPicker.scrollBy(0, 100);
                        } else {
                            numberPicker.performClick();
                            numberPicker.computeScroll();
                        }
                    } else {
                        if (delta >= rowsPerSwipe) {
                            numberPicker.scrollBy(0, -100);
                            numberPicker.computeScroll();
                        } else {
                            numberPicker.performClick();
                        }
                    }
                }
            }
        };
    }

    public static Activity getCurrentActivityInstance() {
        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                CURRENT_ACTIVITY = resumedActivities.iterator().next();
            }
        });
        return CURRENT_ACTIVITY;
    }

    public static void checkPermissionState(final Activity activity) {
        STORAGE.updateRequiredPermissions(activity);
    }

    public static void clearStorage() {
        STORAGE.clear();
    }

    public static Matcher<View> withDate(final Date expectedDate) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                boolean result = false;
                if(item instanceof DateSelectionView) {
                    DateSelectionView dsv = (DateSelectionView) item;
                    Date currentDate = dsv.getDate();
                    result = (expectedDate == null && currentDate == null)
                            || (expectedDate != null && expectedDate.equals(currentDate));
                }
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Item is instance of "
                        + DateSelectionView.class
                        + " and has expected date = "
                        + expectedDate
                );
            }
        };
    }

    public static Matcher<View> isVisible(){
        return withEffectiveVisibility(VISIBLE);
    }
}
