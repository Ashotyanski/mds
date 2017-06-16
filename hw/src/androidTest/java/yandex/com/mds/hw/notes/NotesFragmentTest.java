package yandex.com.mds.hw.notes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.TestUtils;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService;
import yandex.com.mds.hw.notes.synchronizer.conflicts.Utils;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;
import yandex.com.mds.hw.utils.SerializationUtils;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.AllOf.allOf;
import static yandex.com.mds.hw.TestUtils.doWithView;
import static yandex.com.mds.hw.TestUtils.waitFor;
import static yandex.com.mds.hw.colorpicker.ColorPickerDialogTest.nthChildOf;
import static yandex.com.mds.hw.colorpicker.ColorPickerDialogTest.withColor;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class NotesFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private Note note, unmodifiedNote, emptyNote;
    private Date now, later;
    private NoteDao noteDao;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;
    private int userId;
    private Solo mSolo;

    @Before
    public void setUp() throws Exception {
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
        noteDao = new NoteDaoImpl();
        mSolo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());
        now = new Date();
        later = new Date(now.getTime() + 10000);
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);

        unmodifiedNote = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        unmodifiedNote.setServerId(1900);
        unmodifiedNote.setOwnerId(userId);
        note = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        note.setLastViewDate(later);
        note.setLastModificationDate(later);
        note.setServerId(1900);
        note.setOwnerId(userId);
        emptyNote = new Note();
        emptyNote.setOwnerId(userId);

        noteDao.deleteNotes();
        noteDao.addNote(note);
        noteDao.addNote(emptyNote);
        noteDao.addNote(unmodifiedNote);
        mActivityRule.getActivity().getNavigationManager().showNotes();
        mSolo.waitForFragmentByTag("NOTES", 1000);
    }

    @Test
    public void testItemClick() throws Exception {
        onView(withId(R.id.list))
                .perform(doWithView(new TestUtils.OnViewGetInterface() {
                    @Override
                    public void onViewGet(View view) {
                        assertEquals(3, ((ViewGroup) view).getChildCount());
                    }
                }));
        //first note
        onView(withId(R.id.list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        mSolo.waitForView(R.id.color);
        onView(allOf(withId(R.id.title), isDisplayed())).check(matches(withText(note.getTitle())));
        onView(allOf(withId(R.id.description), isDisplayed())).check(matches(withText(note.getDescription())));
        onView(allOf(withId(R.id.color), isDisplayed())).check(matches(withColor(note.getColor())));
        Espresso.pressBack();
        //second note
        onView(withId(R.id.list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        mSolo.waitForView((R.id.color));
        onView(allOf(withId(R.id.title), isDisplayed())).check(matches(withText("")));
        onView(allOf(withId(R.id.description), isDisplayed())).check(matches(withText("")));
        onView(allOf(withId(R.id.color), isDisplayed())).check(matches(withColor(emptyNote.getColor())));
        Espresso.pressBack();
        //third note
        onView(withId(R.id.list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        mSolo.waitForView((R.id.color));
        onView(allOf(withId(R.id.title), isDisplayed())).check(matches(withText(unmodifiedNote.getTitle())));
        onView(allOf(withId(R.id.description), isDisplayed())).check(matches(withText(unmodifiedNote.getDescription())));
        onView(allOf(withId(R.id.color), isDisplayed())).check(matches(withColor(unmodifiedNote.getColor())));
    }

    @Test
    public void testAddButton() throws Exception {
        onView(withId(R.id.fab)).perform(click());
        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_create);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
    }

    @Test
    public void testQueryToggleButton() throws Exception {
        onView(withId(R.id.action_filter)).perform(click()).perform(waitFor(500));
        View v = mActivityRule.getActivity().findViewById(R.id.query);
        assertEquals(View.VISIBLE, v.getVisibility());
        assertEquals(1.0f, v.getAlpha());
        assertTrue(v.getHeight() > 0);
    }

    @Test
    public void testSynchronizationConflict() throws Exception {
        unsynchronizedNotesManager.clear();
        noteDao.deleteNotes();
        note.setId((int) noteDao.addNote(note));
        MockWebServer webServer = new MockWebServer();
        webServer.start();
        webServer.enqueue(new MockResponse().setBody(SerializationUtils.GSON.toJson(
                new NoteServiceResponse<>("ok", new Note[]{}))));
        NoteSynchronizationService.startNoteSynchronizer(mActivityRule.getActivity(), userId,
                webServer.url("/").toString());
        mSolo.waitForText(mActivityRule.getActivity().getString(R.string.sync_conflict_action_resolve));
        onView(withText(R.string.sync_conflict_action_resolve)).perform(click());
        mSolo.waitForFragmentByTag("CONFLICT");
        onView(withContentDescription("Conflict notes")).perform(doWithView(new TestUtils.OnViewGetInterface() {
            @Override
            public void onViewGet(View view) {
                assertEquals(1, ((ViewGroup) view).getChildCount());
            }
        }));
        onView(nthChildOf(withContentDescription("Conflict notes"), 0)).perform(click());
        mSolo.waitForDialogToOpen();
        onView(withId(R.id.text_local))
                .check(matches(withText(Utils.getNotePresentation(mActivityRule.getActivity(), note))));
        onView(withId(R.id.text_remote))
                .check(matches(withText(Utils.getNotePresentation(mActivityRule.getActivity(), null))));
        onView(withId(R.id.button_prefer_remote)).perform(click());
        mSolo.waitForDialogToClose();
        assertEquals(null, noteDao.getNote(note.getId()));
        webServer.shutdown();
    }
}
