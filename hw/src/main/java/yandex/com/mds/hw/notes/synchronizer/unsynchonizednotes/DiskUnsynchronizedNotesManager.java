package yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import yandex.com.mds.hw.MainApplication;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.utils.SerializationUtils;

public class DiskUnsynchronizedNotesManager implements UnsynchronizedNotesManager {
    private UnsynchronizedNotes unsynchronizedNotes;
    private File unsynchronizedNotesPath;

    public DiskUnsynchronizedNotesManager() {
        this(new File(
                MainApplication.getContext().getExternalFilesDir(null), "unsynchronized_notes.json"));
    }

    public DiskUnsynchronizedNotesManager(File unsynchronizedNotesPath) {
        this.unsynchronizedNotesPath = unsynchronizedNotesPath;
        if (!unsynchronizedNotesPath.exists()) {
            try {
                unsynchronizedNotesPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        readFromDisk();
    }

    @Override
    public void putToAdded(Note note) {
        unsynchronizedNotes.getAdded().put(note.getId(), note);
        writeToDisk();
    }

    @Override
    public void putToDeleted(Note note) {
        if (unsynchronizedNotes.getAdded().containsKey(note.getId()))
            unsynchronizedNotes.getAdded().remove(note.getId());
        else if (unsynchronizedNotes.getEdited().containsKey(note.getServerId()))
            unsynchronizedNotes.getEdited().remove(note.getServerId());
        else unsynchronizedNotes.getDeleted().put(note.getServerId(), note);
        writeToDisk();
    }

    @Override
    public void putToEdited(Note note) {
        if (unsynchronizedNotes.getAdded().containsKey(note.getId())) {
            unsynchronizedNotes.getAdded().put(note.getId(), note);
        } else unsynchronizedNotes.getEdited().put(note.getServerId(), note);
        writeToDisk();
    }

    @Override
    public UnsynchronizedNotes getUnsynchronizedNotes(int ownerId) {
        return getUnsynchronizedNotes().extractUnsynchronizedNotesForOwner(ownerId);
    }

    @Override
    public UnsynchronizedNotes getUnsynchronizedNotes() {
        FileReader reader = null;
        try {
            reader = new FileReader(unsynchronizedNotesPath);
            UnsynchronizedNotes unsynchronizedNotes =
                    SerializationUtils.GSON.fromJson(reader, UnsynchronizedNotes.class);
            return unsynchronizedNotes == null ? new UnsynchronizedNotes() : unsynchronizedNotes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new UnsynchronizedNotes();
    }

    @Override
    public void clear() {
        unsynchronizedNotes.getAdded().clear();
        unsynchronizedNotes.getEdited().clear();
        unsynchronizedNotes.getDeleted().clear();
        writeToDisk();
    }

    @Override
    public void remove(Note note) {
        unsynchronizedNotes.getAdded().remove(note.getId());
        unsynchronizedNotes.getEdited().remove(note.getServerId());
        unsynchronizedNotes.getDeleted().remove(note.getServerId());
        writeToDisk();
    }

    private void readFromDisk() {
        try (FileReader reader = new FileReader(unsynchronizedNotesPath)) {
            UnsynchronizedNotes newUnsynchronizedNotes =
                    SerializationUtils.GSON.fromJson(reader, UnsynchronizedNotes.class);
            this.unsynchronizedNotes = newUnsynchronizedNotes == null ? new UnsynchronizedNotes() : newUnsynchronizedNotes;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToDisk() {
        try (FileWriter writer = new FileWriter(unsynchronizedNotesPath)) {
            SerializationUtils.GSON.toJson(unsynchronizedNotes, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
