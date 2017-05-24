package yandex.com.mds.hw.db;

import android.database.Cursor;

import java.util.List;

import yandex.com.mds.hw.notes.query.Query;
import yandex.com.mds.hw.models.Note;

public interface NoteDao {
    List<Note> getNotes();

    List<Note> getNotes(Query query, int userId);

    Cursor getNotesCursor();

    Cursor getNotesCursor(Query query, int userId);

    Note getNote(int id);

    long addNote(Note record);

    boolean addNotes(List<Note> records);

    boolean saveNote(Note record);

    boolean deleteNote(int id);

    boolean deleteNotes();
}
