package yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes;

import java.util.HashMap;
import java.util.Map;

import yandex.com.mds.hw.models.Note;

public class UnsynchronizedNotes {
    private Map<Integer, Note> added = new HashMap<>();
    private Map<Integer, Note> edited = new HashMap<>();
    private Map<Integer, Note> deleted = new HashMap<>();

    public Map<Integer, Note> getAdded() {
        return added;
    }

    public Map<Integer, Note> getEdited() {
        return edited;
    }

    public Map<Integer, Note> getDeleted() {
        return deleted;
    }

    public UnsynchronizedNotes extractUnsynchronizedNotesForOwner(int ownerId) {
        UnsynchronizedNotes notes = new UnsynchronizedNotes();
        for (Map.Entry<Integer, Note> entry : added.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.added.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Note> entry : edited.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.edited.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Note> entry : deleted.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.deleted.put(entry.getKey(), entry.getValue());
        }
        return notes;
    }
}
