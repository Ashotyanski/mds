package yandex.com.mds.hw.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yandex.com.mds.hw.models.Note;

import static android.provider.BaseColumns._ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.COLOR;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.DESCRIPTION;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.IMAGE_URL;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.OWNER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.SERVER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TITLE;
import static yandex.com.mds.hw.utils.TimeUtils.trimMilliseconds;

@RunWith(AndroidJUnit4.class)
public class NoteDaoImplTest {
    private NoteDao noteDao;
    private NoteDatabaseHelper helper;
    private Note note, unmodifiedNote, emptyNote;
    private Date now, later;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws Exception {
        noteDao = new NoteDaoImpl();
        helper = NoteDatabaseHelper.getInstance(InstrumentationRegistry.getTargetContext());
        helper.clean();
        db = helper.getReadableDatabase();

        now = new Date();
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
    public void testAddNote() throws Exception {
        long id = noteDao.addNote(emptyNote);
        Cursor c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, _ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        assertEquals(id, c.getLong(c.getColumnIndex(_ID)));
        assertEquals(emptyNote.getTitle(), c.getString(c.getColumnIndex(TITLE)));
        assertEquals(emptyNote.getDescription(), c.getString(c.getColumnIndex(DESCRIPTION)));
        assertEquals(emptyNote.getColor(), c.getInt(c.getColumnIndex(COLOR)));
        assertEquals(emptyNote.getImageUrl(), c.getString(c.getColumnIndex(IMAGE_URL)));
        c.close();
        helper.clean();

        id = noteDao.addNote(note);
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, _ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        assertEquals(id, c.getLong(c.getColumnIndex(_ID)));
        assertEquals(note.getTitle(), c.getString(c.getColumnIndex(TITLE)));
        assertEquals(note.getDescription(), c.getString(c.getColumnIndex(DESCRIPTION)));
        assertEquals(note.getColor(), c.getInt(c.getColumnIndex(COLOR)));
        assertEquals(note.getOwnerId(), c.getInt(c.getColumnIndex(OWNER_ID)));
        assertEquals(note.getServerId(), c.getInt(c.getColumnIndex(SERVER_ID)));
        assertEquals(note.getImageUrl(), c.getString(c.getColumnIndex(IMAGE_URL)));
        assertEquals(trimMilliseconds(note.getCreationDate()).getTime(),
                c.getLong(c.getColumnIndex(CREATION_DATE)));
        assertEquals(trimMilliseconds(note.getLastModificationDate()).getTime(),
                c.getLong(c.getColumnIndex(LAST_MODIFICATION_DATE)));
        assertEquals(trimMilliseconds(note.getLastViewDate()).getTime(),
                c.getLong(c.getColumnIndex(LAST_VIEW_DATE)));

        long newId = noteDao.addNote(note);
        assertNotSame(id, newId);
        c.close();
    }

    @Test
    public void testAddNotes() throws Exception {
        List<Note> notes = new ArrayList<>();
        noteDao.addNotes(notes);
        Cursor c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(0, c.getCount());
        c.close();

        notes.add(emptyNote);
        notes.add(note);
        noteDao.addNotes(notes);
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(2, c.getCount());
        //first note
        assertEquals(emptyNote.getTitle(), c.getString(c.getColumnIndex(TITLE)));
        assertEquals(emptyNote.getDescription(), c.getString(c.getColumnIndex(DESCRIPTION)));
        assertEquals(emptyNote.getColor(), c.getInt(c.getColumnIndex(COLOR)));
        assertEquals(emptyNote.getOwnerId(), c.getInt(c.getColumnIndex(OWNER_ID)));
        assertEquals(emptyNote.getServerId(), c.getInt(c.getColumnIndex(SERVER_ID)));
        assertEquals(emptyNote.getImageUrl(), c.getString(c.getColumnIndex(IMAGE_URL)));
        //second note
        c.moveToNext();
        assertEquals(note.getTitle(), c.getString(c.getColumnIndex(TITLE)));
        assertEquals(note.getDescription(), c.getString(c.getColumnIndex(DESCRIPTION)));
        assertEquals(note.getColor(), c.getInt(c.getColumnIndex(COLOR)));
        assertEquals(note.getOwnerId(), c.getInt(c.getColumnIndex(OWNER_ID)));
        assertEquals(note.getServerId(), c.getInt(c.getColumnIndex(SERVER_ID)));
        assertEquals(note.getImageUrl(), c.getString(c.getColumnIndex(IMAGE_URL)));
        assertEquals(trimMilliseconds(note.getCreationDate()).getTime(),
                c.getLong(c.getColumnIndex(CREATION_DATE)));
        assertEquals(trimMilliseconds(note.getLastModificationDate()).getTime(),
                c.getLong(c.getColumnIndex(LAST_MODIFICATION_DATE)));
        assertEquals(trimMilliseconds(note.getLastViewDate()).getTime(),
                c.getLong(c.getColumnIndex(LAST_VIEW_DATE)));
        c.close();
    }

    @Test
    public void testDeleteNote() {
        long noteId = noteDao.addNote(note);
        long emptyNoteId = noteDao.addNote(emptyNote);
        long unmodifiedNoteId = noteDao.addNote(unmodifiedNote);

        Cursor c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(3, c.getCount());
        c.close();

        noteDao.deleteNote((int) noteId);
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(2, c.getCount());
        assertEquals(emptyNoteId, c.getInt(c.getColumnIndex(_ID)));
        c.moveToNext();
        assertEquals(unmodifiedNoteId, c.getInt(c.getColumnIndex(_ID)));
        c.close();

        noteDao.deleteNote((int) unmodifiedNoteId);
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(1, c.getCount());
        assertEquals(emptyNoteId, c.getInt(c.getColumnIndex(_ID)));
        c.close();

        noteDao.deleteNote((int) emptyNoteId);
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(0, c.getCount());
        c.close();

        assertFalse(noteDao.deleteNote((int) emptyNoteId));
    }

    @Test
    public void testDeleteNotes() {
        noteDao.addNote(note);
        noteDao.addNote(note);
        noteDao.addNote(note);

        Cursor c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(3, c.getCount());
        c.close();

        noteDao.deleteNotes();
        c = db.query(TABLE_NAME, NoteDatabaseHelper.ALL_COLUMNS, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(0, c.getCount());
        c.close();
    }

    @Test
    public void testGetNote() {
        assertNull(noteDao.getNote(1));
        assertNull(noteDao.getNote(-1));
        note.setTitle("1");
        emptyNote.setTitle("2");
        unmodifiedNote.setTitle("3");
        long noteId = noteDao.addNote(note);
        long emptyNoteId = noteDao.addNote(emptyNote);
        long unmodifiedNoteId = noteDao.addNote(unmodifiedNote);
        assertEquals("1", noteDao.getNote((int) noteId).getTitle());
        assertEquals("2", noteDao.getNote((int) emptyNoteId).getTitle());
        assertEquals("3", noteDao.getNote((int) unmodifiedNoteId).getTitle());
    }

    @Test
    public void testSaveNote() throws Exception {
        assertFalse(noteDao.saveNote(note));
        note.setTitle("1");
        long id = noteDao.addNote(note);
        assertEquals("1", noteDao.getNote((int) id).getTitle());

        note.setId((int) id);
        note.setTitle("updated 1");
        noteDao.saveNote(note);

        assertEquals("updated 1", noteDao.getNote((int) id).getTitle());
    }
}
