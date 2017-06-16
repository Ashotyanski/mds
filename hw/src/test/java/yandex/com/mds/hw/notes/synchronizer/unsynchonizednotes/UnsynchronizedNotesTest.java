package yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import yandex.com.mds.hw.models.Note;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class UnsynchronizedNotesTest {
    static final int OWNER_ONE = 1;
    static final int OWNER_TWO = 2;
    static final int OWNER_ANY = 777;

    @Test
    public void testExtractUnsynchronizedNotesForOwner() throws Exception {
        UnsynchronizedNotes unsynchronizedNotes = new UnsynchronizedNotes();
        unsynchronizedNotes.getAdded().put(1, new Note(1, OWNER_ONE, 1, "1", "1", new Date(), "1.jpg"));
        unsynchronizedNotes.getDeleted().put(2, new Note(2, OWNER_TWO, 2, "2", "2", new Date(), "2.jpg"));
        unsynchronizedNotes.getAdded().put(3, new Note(3, OWNER_ANY, 3, "3", "3", new Date(), "3.jpg"));
        unsynchronizedNotes.getDeleted().put(4, new Note(4, OWNER_ANY, 4, "4", "4", new Date(), "4.jpg"));
        unsynchronizedNotes.getEdited().put(5, new Note(5, OWNER_TWO, 5, "5", "5", new Date(), "5.jpg"));

        UnsynchronizedNotes notesForOne = unsynchronizedNotes.extractUnsynchronizedNotesForOwner(OWNER_ONE);
        assertTrue(notesForOne.getAdded().size() == 1 && notesForOne.getDeleted().size() == 0 &&
                notesForOne.getEdited().size() == 0 && notesForOne.getAdded().get(1).getOwnerId() == OWNER_ONE);
        UnsynchronizedNotes notesForTwo = unsynchronizedNotes.extractUnsynchronizedNotesForOwner(OWNER_TWO);
        assertTrue(notesForTwo.getAdded().size() == 0 && notesForTwo.getDeleted().size() == 1 &&
                notesForTwo.getEdited().size() == 1 && notesForTwo.getEdited().get(5).getOwnerId() == OWNER_TWO &&
                notesForTwo.getDeleted().get(2).getOwnerId() == OWNER_TWO);
    }

    @Test
    public void testExtractEmptyUnsynchronizedNotesForOwnerNotNull() throws Exception {
        UnsynchronizedNotes unsynchronizedNotes = new UnsynchronizedNotes();
        UnsynchronizedNotes notesForOwner = unsynchronizedNotes.extractUnsynchronizedNotesForOwner(OWNER_ANY);
        assertNotNull(notesForOwner);
        assertNotNull(notesForOwner.getAdded());
        assertNotNull(notesForOwner.getDeleted());
        assertNotNull(notesForOwner.getEdited());
    }
}
