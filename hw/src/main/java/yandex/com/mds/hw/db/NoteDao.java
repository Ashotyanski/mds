package yandex.com.mds.hw.db;

import android.database.Cursor;

import yandex.com.mds.hw.notes.query.Query;
import yandex.com.mds.hw.models.Note;

public interface NoteDao {
    Note[] getNotes();

    Note[] getNotes(Query query, int userId);

    Cursor getNotesCursor();

    Cursor getNotesCursor(Query query, int userId);

    Note getNote(int id);

    long addNote(Note record);

    boolean addNotes(Note[] records);

    boolean saveNote(Note record);

    boolean deleteNote(int id);

    boolean deleteNotes();
}
