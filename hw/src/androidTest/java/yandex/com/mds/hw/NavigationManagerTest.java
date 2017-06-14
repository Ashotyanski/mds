package yandex.com.mds.hw;

import android.os.Looper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.navigation.NavigationManager;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class NavigationManagerTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private NavigationManager manager;
    private NoteDao noteDao;
    private Note note;
    private int userId;

    @Before
    public void setUp() throws Exception {
        manager = mActivityRule.getActivity().getNavigationManager();
        noteDao = new NoteDaoImpl();
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);
        note = new Note();
        note.setTitle("test");
        note.setOwnerId(userId);
    }

    @Test
    public void testNotesPage() throws Exception {
        manager.showNotes();
        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_notes);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
    }

    @Test
    public void testNotesEditPage() throws Exception {
        int id = (int) noteDao.addNote(note);

        manager.showNoteEdit(id, userId, null);

        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_edit);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
        onView(withId(R.id.title))
                .check(matches(withText(note.getTitle())));
    }

    @Test
    public void testNotesPager() throws Exception {
        int id = (int) noteDao.addNote(note);
        note.setId(id);

        ArrayList<Note> notes = new ArrayList<>();
        notes.add(note);
        manager.showNotePager(0, notes, null);

        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_edit);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
        onView(withId(R.id.title))
                .check(matches(withText(note.getTitle())));
    }

    @Test
    public void testNotesAddPage() throws Exception {
        manager.showNoteAdd(userId);
        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_create);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
    }

    @Test
    public void testNotesImportExportPage() throws Exception {
        Looper.prepare();
        manager.showNotesImportExport();
        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_import_export);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
    }

    @Test
    public void testNavigateBack() throws Exception {
        manager.showNotes();
        String toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_notes);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));

        manager.showNoteAdd(userId);
        toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_note_create);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));

        manager.navigateBack(mActivityRule.getActivity());
        toolbarTitle = getInstrumentation().getTargetContext().getString(R.string.title_notes);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class))))
                .check(matches(withText(toolbarTitle)));
    }
}