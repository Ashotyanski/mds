package yandex.com.mds.hw.notes.synchronizer;

import java.util.HashMap;
import java.util.Map;

import yandex.com.mds.hw.models.Note;

public class UnsynchronizedNotes {
    Map<Integer, Note> added = new HashMap<>();
    Map<Integer, Note> edited = new HashMap<>();
    Map<Integer, Note> deleted = new HashMap<>();

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
