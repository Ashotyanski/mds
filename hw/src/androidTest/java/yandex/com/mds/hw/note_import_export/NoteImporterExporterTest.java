package yandex.com.mds.hw.note_import_export;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class NoteImporterExporterTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private NoteImporterExporter exporter;
    private NoteDao noteDao;
    private Note note, unmodifiedNote, emptyNote;
    private Date now, later;
    private CountDownLatch latch;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();

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
    public void testImportExport() throws Exception {
        exporter = NoteImporterExporter.getInstance(mActivityRule.getActivity());
        noteDao.deleteNotes();
        noteDao.addNote(emptyNote);
        noteDao.addNote(note);
        noteDao.addNote(unmodifiedNote);
        List<Note> notes = noteDao.getNotes();
        assertEquals(3, notes.size());

        latch = new CountDownLatch(1);
        exporter.setExportListener(new NoteImporterExporter.OnColorsExportListener() {
            @Override
            public void OnColorsExport(NoteImporterExporter.ImportExportStatus status) {
                if (status.progress == 1.0)
                    latch.countDown();
                else if (status.progress < 0)
                    fail();
            }
        });
        exporter.exportNotes("notes.json");
        latch.await();
        noteDao.deleteNotes();
        latch = new CountDownLatch(1);
        exporter.setImportListener(new NoteImporterExporter.OnColorsImportListener() {
            @Override
            public void OnColorsImport(NoteImporterExporter.ImportExportStatus status) {
                if (status.progress == 1.0)
                    latch.countDown();
                else if (status.progress < 0)
                    fail();
            }
        });
        exporter.importNotes("notes.json");
        latch.await(1, TimeUnit.SECONDS);
        List<Note> newNotes = noteDao.getNotes();
        assertEquals(notes, newNotes);
    }
}
