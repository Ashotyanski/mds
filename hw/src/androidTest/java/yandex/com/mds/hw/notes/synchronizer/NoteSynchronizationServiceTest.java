package yandex.com.mds.hw.notes.synchronizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.notes.synchronizer.conflicts.ConflictNotes;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;
import yandex.com.mds.hw.utils.SerializationUtils;

import static android.content.Context.MODE_PRIVATE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_COMPLETE_ACTION;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_CONFLICT_NOTES;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_ILLEGAL_FORMAT;

@RunWith(AndroidJUnit4.class)
public class NoteSynchronizationServiceTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private MockWebServer webServer;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;
    private LocalBroadcastManager broadcastManager;
    private NoteDao noteDao;
    private Note note;
    private Note note2;
    private Date now, later;
    private int userId;
    private CountDownLatch latch;
    private BroadcastReceiver receiver;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();
        noteDao.deleteNotes();
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
        unsynchronizedNotesManager.clear();
        webServer = new MockWebServer();
        webServer.start();
        broadcastManager = LocalBroadcastManager.getInstance(mActivityRule.getActivity());
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);

        now = new Date();
        later = new Date(now.getTime() + 10000);

        note = new Note(1, userId, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        note.setLastViewDate(later);
        note.setLastModificationDate(later);

        note2 = new Note(2, userId, 0x0000000F, "title2", "descr2", now, "imgur2.jpg");
        note2.setLastViewDate(later);
        note2.setLastModificationDate(later);
    }

    @Test
    public void testSynchronization() throws Exception {
        int noteId = (int) noteDao.addNote(note);
        note.setId(noteId);
        unsynchronizedNotesManager.putToAdded(note);

        webServer.enqueue(new MockResponse().setBody(SerializationUtils.GSON.toJson(
                new NoteServiceResponse<>("ok", new Note[]{note2})))
        );
        webServer.enqueue(new MockResponse().setBody(SerializationUtils.GSON.toJson(
                new NoteServiceResponse<>("ok", 777)))
        );

        latch = new CountDownLatch(1);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle extras = intent.getExtras();
                if (extras != null)
                    fail();
                else
                    latch.countDown();
            }
        };
        broadcastManager.registerReceiver(receiver, new IntentFilter(SYNC_COMPLETE_ACTION));
        NoteSynchronizationService.startNoteSynchronizer(mActivityRule.getActivity(), userId,
                webServer.url("/").toString());
        latch.await();
        assertEquals(777, noteDao.getNote(noteId).getServerId());
        assertEquals(2, noteDao.getNotes(null, userId).size());
    }

    @Test
    public void testSynchronizationWithConflict() throws Exception {
        note.setServerId(777);
        noteDao.addNote(note);

        webServer.enqueue(new MockResponse().setBody(SerializationUtils.GSON.toJson(
                new NoteServiceResponse<>("ok", new Note[]{note2})))
        );
        webServer.enqueue(new MockResponse().setBody(SerializationUtils.GSON.toJson(
                new NoteServiceResponse<>("ok", null)))
        );
        latch = new CountDownLatch(1);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.getBoolean(SYNC_ILLEGAL_FORMAT)) {
                        fail();
                    }
                    if (extras.<ConflictNotes>getParcelableArrayList(SYNC_CONFLICT_NOTES) != null) {
                        ArrayList<ConflictNotes> conflictNotes = extras.getParcelableArrayList(SYNC_CONFLICT_NOTES);
                        assertEquals(1, conflictNotes.size());
                        assertEquals(noteDao.getNotes().get(0), conflictNotes.get(0).getLocal());
                        assertNull(conflictNotes.get(0).getRemote());
                        latch.countDown();
                    } else {
                        fail();
                    }
                } else fail();
            }
        };
        broadcastManager.registerReceiver(receiver, new IntentFilter(SYNC_COMPLETE_ACTION));
        NoteSynchronizationService.startNoteSynchronizer(mActivityRule.getActivity(), userId,
                webServer.url("/").toString());
        latch.await();

    }

    @After
    public void tearDown() throws Exception {
        webServer.shutdown();
        broadcastManager.unregisterReceiver(receiver);
    }
}
