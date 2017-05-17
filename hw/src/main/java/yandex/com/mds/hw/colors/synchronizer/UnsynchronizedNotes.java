package yandex.com.mds.hw.colors.synchronizer;

import java.util.HashMap;
import java.util.Map;

import yandex.com.mds.hw.models.ColorRecord;

public class UnsynchronizedNotes {
    Map<Integer, ColorRecord> added = new HashMap<>();
    Map<Integer, ColorRecord> edited = new HashMap<>();
    Map<Integer, ColorRecord> deleted = new HashMap<>();

    public UnsynchronizedNotes extractUnsynchronizedNotesForOwner(int ownerId) {
        UnsynchronizedNotes notes = new UnsynchronizedNotes();
        for (Map.Entry<Integer, ColorRecord> entry : added.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.added.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, ColorRecord> entry : edited.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.edited.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, ColorRecord> entry : deleted.entrySet()) {
            if (entry.getValue().getOwnerId() == ownerId)
                notes.deleted.put(entry.getKey(), entry.getValue());
        }
        return notes;
    }
}
