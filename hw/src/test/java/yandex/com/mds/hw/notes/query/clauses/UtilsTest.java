package yandex.com.mds.hw.notes.query.clauses;

import org.junit.Test;

import yandex.com.mds.hw.notes.query.Utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TITLE;

public class UtilsTest {
    private static final String RANDOM_KEY = "af31gd3v";

    @Test
    public void testGetDateFilterFieldPosition() throws Exception {
        assertEquals(0, Utils.getDateFilterFieldPosition("creation date"));
        assertEquals(1, Utils.getDateFilterFieldPosition("last modification date"));
        assertEquals(2, Utils.getDateFilterFieldPosition("last view date"));
        assertEquals(-1, Utils.getDateFilterFieldPosition(null));
        assertEquals(-1, Utils.getDateFilterFieldPosition(RANDOM_KEY));
    }

    @Test
    public void testGetSortFieldPosition() throws Exception {
        assertEquals(0, Utils.getSortFieldPosition("title"));
        assertEquals(1, Utils.getSortFieldPosition("creation date"));
        assertEquals(2, Utils.getSortFieldPosition("last modification date"));
        assertEquals(3, Utils.getSortFieldPosition("last view date"));
        assertEquals(-1, Utils.getSortFieldPosition(null));
        assertEquals(-1, Utils.getSortFieldPosition(RANDOM_KEY));
    }

    @Test
    public void testGetDbColumnFromField() throws Exception {
        assertEquals(TITLE, Utils.getDbColumnFromField("title"));
        assertEquals(CREATION_DATE, Utils.getDbColumnFromField("creation date"));
        assertEquals(LAST_MODIFICATION_DATE, Utils.getDbColumnFromField("last modification date"));
        assertEquals(LAST_VIEW_DATE, Utils.getDbColumnFromField("last view date"));
        assertNull(Utils.getDbColumnFromField(null));
        assertNull(Utils.getDbColumnFromField(RANDOM_KEY));
    }
}
