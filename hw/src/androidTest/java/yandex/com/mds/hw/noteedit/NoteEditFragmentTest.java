package yandex.com.mds.hw.noteedit;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.ButtonBarLayout;
import android.widget.LinearLayout;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.navigation.NavigationManager;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.AllOf.allOf;
import static yandex.com.mds.hw.TestUtils.waitFor;
import static yandex.com.mds.hw.colorpicker.ColorPickerDialogTest.nthChildOf;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class NoteEditFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private Note unmodifiedNote, emptyNote;
    private Date now, later;
    private NoteDao noteDao;
    private NavigationManager manager;
    private int userId;
    private Solo mSolo;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);
        manager = mActivityRule.getActivity().getNavigationManager();
        mSolo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());
        now = new Date();
        later = new Date(now.getTime() + 10000);

        unmodifiedNote = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        unmodifiedNote.setServerId(1900);

        emptyNote = new Note();

        noteDao.deleteNotes();
    }

    @Test
    public void testCreateNote() throws Exception {
        onView(withId(R.id.fab)).perform(click());
        //set image
        onView(withId(R.id.url_image))
                .perform(click());
        onView(withContentDescription("Image URL"))
                .check(matches(isDisplayed()))
                .perform(replaceText("notes"));
        onView(allOf(withParent(isAssignableFrom(ButtonBarLayout.class)), withId(android.R.id.button1)))
                .perform(click());
        //set title
        onView(withId(R.id.title)).perform(typeText(("Title")));
        //set description
        onView(withId(R.id.description)).perform(typeText(("Description")));
        //set color
        onView(withId(R.id.color))
                .perform(click());
        mSolo.waitForDialogToOpen();
        onView(nthChildOf(allOf(
                isAssignableFrom(LinearLayout.class),
                isDescendantOfA(withId(R.id.color_picker_view))), 0))
                .perform(click())
                .perform(waitFor(100));
        onView(withId(R.id.pick_button)).perform(click());
        //save
        onView(withId(R.id.save_button)).perform(click());

        List<Note> notes = noteDao.getNotes();
        assertEquals(1, notes.size());
        Note note = notes.get(0);
        assertEquals("Title", note.getTitle());
        assertEquals("Description", note.getDescription());
        assertEquals(-53504, note.getColor());
        assertEquals("notes", note.getImageUrl());
        assertEquals(userId, note.getOwnerId());
    }

    @Test
    public void testDeleteNote() throws Exception {
        emptyNote.setOwnerId(userId);
        noteDao.addNote(emptyNote);
        manager.showNotes();
        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.action_delete)).perform(click());
        List<Note> notes = noteDao.getNotes();
        assertEquals(0, notes.size());
    }

    @Test
    public void testSaveNote() throws Exception {
        emptyNote.setOwnerId(userId);
        noteDao.addNote(emptyNote);
        manager.showNotes();
        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //set image
        onView(withId(R.id.url_image))
                .perform(click());
        onView(withContentDescription("Image URL"))
                .check(matches(isDisplayed()))
                .perform(replaceText("notes"));
        onView(allOf(withParent(isAssignableFrom(ButtonBarLayout.class)), withId(android.R.id.button1)))
                .perform(click());
        //set title
        onView(withId(R.id.title)).perform(typeText(("Title")));
        //set description
        onView(withId(R.id.description)).perform(typeText(("Description")));
        //set color
        onView(withId(R.id.color))
                .perform(click());
        mSolo.waitForDialogToOpen(100);
        onView(nthChildOf(allOf(
                isAssignableFrom(LinearLayout.class),
                isDescendantOfA(withId(R.id.color_picker_view))), 0))
                .perform(click())
                .perform(waitFor(100));
        onView(withId(R.id.pick_button)).perform(click());
        //save
        onView(withId(R.id.save_button)).perform(click());

        List<Note> notes = noteDao.getNotes();
        assertEquals(1, notes.size());
        Note note = notes.get(0);
        assertEquals("Title", note.getTitle());
        assertEquals("Description", note.getDescription());
        assertEquals(-53504, note.getColor());
        assertEquals("notes", note.getImageUrl());
    }
}
