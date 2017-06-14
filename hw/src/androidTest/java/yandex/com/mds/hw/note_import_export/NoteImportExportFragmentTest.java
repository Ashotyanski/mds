package yandex.com.mds.hw.note_import_export;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NoteImportExportFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private NoteDao noteDao;
    private Note note, unmodifiedNote, emptyNote;
    private Date now, later;
    private Solo mSolo;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();
        mSolo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());

        now = new Date(1000000);
        later = new Date(now.getTime() + 10000);
        unmodifiedNote = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        unmodifiedNote.setServerId(1900);

        note = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        note.setLastViewDate(later);
        note.setLastModificationDate(later);
        note.setServerId(1900);

        emptyNote = new Note();
    }

    @Test
    public void testImportExportUi() throws Exception {
        mActivityRule.getActivity().getNavigationManager().showNotesImportExport();
        noteDao.deleteNotes();
        noteDao.addNote(note);
        noteDao.addNote(emptyNote);
        noteDao.addNote(unmodifiedNote);
        List<Note> notes = noteDao.getNotes();

        onView(withText(R.string.pref_title_export)).perform(click());
        mSolo.waitForText(mActivityRule.getActivity().getString(R.string.success_notes_export), 1, 1000);
        onView(withText(R.string.pref_title_import))
                .perform(click());

        List<Note> newNotes = noteDao.getNotes();
        assertEquals(notes, newNotes);
    }
}