package yandex.com.mds.hw.colorpicker;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.robotium.solo.Solo;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.TestUtils;
import yandex.com.mds.hw.colorpicker.colorview.ColorView;
import yandex.com.mds.hw.colorpicker.colorview.EditableColorView;
import yandex.com.mds.hw.navigation.NavigationManager;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.AllOf.allOf;
import static yandex.com.mds.hw.TestUtils.doWithView;
import static yandex.com.mds.hw.TestUtils.waitFor;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class ColorPickerDialogTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private NavigationManager manager;
    private int userId;
    private LinearLayout pickerView;
    private Solo mSolo;

    @Before
    public void setUp() throws Exception {
        manager = mActivityRule.getActivity().getNavigationManager();
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);
        mSolo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());
    }

    @Test
    public void testColorPickerDialog() throws Exception {
        manager.showNoteAdd(userId);
        mSolo.waitForFragmentByTag("NOTE_ADD", 100);
        onView(withId(R.id.color)).perform(click());
        mSolo.waitForDialogToOpen(500);
        onView(allOf(isAssignableFrom(LinearLayout.class), isDescendantOfA(withId(R.id.color_picker_view))))
                .check(matches(isDisplayed()))
                .perform(doWithView(new TestUtils.OnViewGetInterface() {
                    @Override
                    public void onViewGet(View view) {
                        pickerView = (LinearLayout) view;
                    }
                }));
        int color;
        for (int i = 0; i < 16; i++) {
            color = ((EditableColorView) pickerView.getChildAt(i)).getColor();
            onView(nthChildOf(allOf(
                    isAssignableFrom(LinearLayout.class),
                    isDescendantOfA(withId(R.id.color_picker_view))), i))
                    .perform(scrollTo())
                    .perform(click());
            onView(withId(R.id.color_view))
                    .perform(waitFor(100))
                    .check(matches(withColor(color)));
        }
    }

    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("position " + childPosition + " of parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) return false;
                ViewGroup parent = (ViewGroup) view.getParent();
                return parentMatcher.matches(parent)
                        && parent.getChildCount() > childPosition
                        && parent.getChildAt(childPosition).equals(view);
            }
        };
    }

    public static Matcher<View> withColor(final int color) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("color " + color);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (view instanceof ColorView) {
                    int actualColor = ((ColorView) view).getColor();
                    assertEquals(color, actualColor);
                    if (color == actualColor) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
