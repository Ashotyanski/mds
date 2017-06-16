package yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes;

import yandex.com.mds.hw.models.Note;

public interface UnsynchronizedNotesManager {
    void putToAdded(Note note);

    void putToDeleted(Note note);

    void putToEdited(Note note);

    UnsynchronizedNotes getUnsynchronizedNotes(int ownerId);

    UnsynchronizedNotes getUnsynchronizedNotes();

    void clear();

    void remove(Note note);
}
