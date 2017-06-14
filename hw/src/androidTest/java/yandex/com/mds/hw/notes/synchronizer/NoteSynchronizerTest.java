package yandex.com.mds.hw.notes.synchronizer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.notes.synchronizer.conflicts.ConflictNotes;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;
import yandex.com.mds.hw.utils.SerializationUtils;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class NoteSynchronizerTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private MockWebServer webServer;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;
    private NoteDao noteDao;
    private Note note;
    private Note note2;
    private Date now, later;
    private int userId;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
        webServer = new MockWebServer();
        webServer.start();
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);

        now = new Date();
        later = new Date(now.getTime() + 10000);

        note = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        note.setLastViewDate(later);
        note.setLastModificationDate(later);
        note.setServerId(1900);
        note.setOwnerId(userId);

        note2 = new Note(2, 10, 0x00000000, "title2", "descr2", now, "imgur2.jpg");
        note2.setLastViewDate(later);
        note2.setLastModificationDate(later);

        noteDao.deleteNotes();
        noteDao.addNote(note);
    }

    @Test
    public void testNoteSynchronizer() throws Exception {
        unsynchronizedNotesManager.clear();
        webServer.enqueue(
                new MockResponse().setBody(SerializationUtils.GSON_SERVER.toJson(
                        new NoteServiceResponse<>("ok", new Note[]{note2}))
                )
        );
        NoteService service = ServiceGenerator.createService(NoteService.class, webServer.url("/").toString());
        NoteSynchronizer synchronizer = new NoteSynchronizer(mActivityRule.getActivity(), service,
                new DiskUnsynchronizedNotesManager());
        List<ConflictNotes> conflictNotes = synchronizer.synchronize(userId);
        assertEquals(noteDao.getNotes(null, userId).get(0), conflictNotes.get(0).getLocal());
        assertNull(conflictNotes.get(0).getRemote());
        assertEquals(2, noteDao.getNotes(null, userId).size());
    }

    @Test
    public void testNoteSynchronizerWithConflict() throws Exception {
        webServer.enqueue(
                new MockResponse().setBody(SerializationUtils.GSON_SERVER.toJson(
                        new NoteServiceResponse<>("ok", new Note[]{}))
                )
        );
        NoteSynchronizer synchronizer = new NoteSynchronizer(mActivityRule.getActivity(),
                ServiceGenerator.createService(NoteService.class, webServer.url("/").toString()),
                new DiskUnsynchronizedNotesManager());
        List<ConflictNotes> conflictNotes = synchronizer.synchronize(userId);
        assertEquals(noteDao.getNotes().get(0), conflictNotes.get(0).getLocal());
        assertNull(conflictNotes.get(0).getRemote());
    }

    @After
    public void tearDown() throws Exception {
        webServer.shutdown();
    }
}