package yandex.com.mds.hw.db;

import android.content.ContentValues;
import android.database.MatrixCursor;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import yandex.com.mds.hw.models.Note;

import static android.provider.BaseColumns._ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.COLOR;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.DESCRIPTION;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.IMAGE_URL;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.OWNER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.SERVER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TITLE;
import static yandex.com.mds.hw.utils.TimeUtils.trimMilliseconds;

@RunWith(AndroidJUnit4.class)
public class NoteDatabaseHelperTest {
    private Date now, later;
    private Note unmodifiedNote, note;

    @Before
    public void setUp() throws Exception {
        now = trimMilliseconds(new Date());
        later = new Date(now.getTime() + 10000);
        unmodifiedNote = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        unmodifiedNote.setServerId(1900);

        note = new Note(1, 10, 0xFFFFFFFF, "title", "descr", now, "imgur.jpg");
        note.setLastViewDate(later);
        note.setLastModificationDate(later);
        note.setServerId(1900);
    }

    @Test
    public void testToContentValuesFromRecordWithCreate() throws Exception {
        ContentValues values = NoteDatabaseHelper.toContentValues(unmodifiedNote, false);
        assertEquals(unmodifiedNote.getOwnerId(), values.getAsInteger(OWNER_ID).intValue());
        assertNull(values.get(_ID));
        assertEquals(unmodifiedNote.getServerId(), values.getAsInteger(SERVER_ID).intValue());
        assertEquals(unmodifiedNote.getColor(), values.getAsInteger(COLOR).intValue());
        assertEquals(unmodifiedNote.getTitle(), values.getAsString(TITLE));
        assertEquals(unmodifiedNote.getDescription(), values.getAsString(DESCRIPTION));
        assertEquals(unmodifiedNote.getCreationDate().getTime(), values.getAsLong(CREATION_DATE).longValue());
        assertNull(values.get(LAST_MODIFICATION_DATE));
        assertNull(values.get(LAST_VIEW_DATE));
        assertEquals(unmodifiedNote.getImageUrl(), values.getAsString(IMAGE_URL));

        unmodifiedNote.setLastModificationDate(later);
        unmodifiedNote.setLastViewDate(later);
        values = NoteDatabaseHelper.toContentValues(unmodifiedNote, false);
        assertEquals(unmodifiedNote.getLastViewDate().getTime(), values.getAsLong(LAST_VIEW_DATE).longValue());
        assertEquals(unmodifiedNote.getLastModificationDate().getTime(), values.getAsLong(LAST_MODIFICATION_DATE).longValue());
    }

    @Test
    public void testToContentValuesFromRecordWithUpdate() throws Exception {
        ContentValues values = NoteDatabaseHelper.toContentValues(unmodifiedNote, true);
        assertEquals(1, values.getAsInteger(_ID).intValue());
        assertNull(values.get(OWNER_ID));
        assertEquals(unmodifiedNote.getServerId(), values.getAsInteger(SERVER_ID).intValue());
        assertEquals(unmodifiedNote.getColor(), values.getAsInteger(COLOR).intValue());
        assertEquals(unmodifiedNote.getTitle(), values.getAsString(TITLE));
        assertEquals(unmodifiedNote.getDescription(), values.getAsString(DESCRIPTION));
        assertEquals(unmodifiedNote.getCreationDate().getTime(), values.getAsLong(CREATION_DATE).longValue());
        assertNull(values.get(LAST_MODIFICATION_DATE));
        assertNull(values.get(LAST_VIEW_DATE));
        assertEquals(unmodifiedNote.getImageUrl(), values.getAsString(IMAGE_URL));

        unmodifiedNote.setLastModificationDate(later);
        unmodifiedNote.setLastViewDate(later);
        values = NoteDatabaseHelper.toContentValues(unmodifiedNote, true);
        assertEquals(unmodifiedNote.getLastViewDate().getTime(), values.getAsLong(LAST_VIEW_DATE).longValue());
        assertEquals(unmodifiedNote.getLastModificationDate().getTime(), values.getAsLong(LAST_MODIFICATION_DATE).longValue());
    }

    @Test
    public void testToRecordFromCursor() throws Exception {
        MatrixCursor cursor = new MatrixCursor(NoteDatabaseHelper.ALL_COLUMNS);
        cursor.addRow(new Object[]{
                note.getId(), note.getTitle(), note.getDescription(), note.getColor(),
                note.getCreationDate().getTime(), note.getLastModificationDate().getTime(), note.getLastViewDate().getTime(),
                note.getImageUrl(), note.getOwnerId(), note.getServerId()
        });
        cursor.moveToFirst();
        Note noteFromCursor = NoteDatabaseHelper.toRecord(cursor);
        Assert.assertEquals(note, noteFromCursor);
    }
}