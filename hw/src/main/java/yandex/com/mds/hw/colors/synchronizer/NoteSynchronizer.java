package yandex.com.mds.hw.colors.synchronizer;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import yandex.com.mds.hw.MainApplication;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.utils.NetworkUtils;
import yandex.com.mds.hw.utils.SerializationUtils;

public class NoteSynchronizer {
    private static final String TAG = NoteSynchronizer.class.getName();
    public static final String SYNC_CONFLICT_ACTION = "SYNC_CONFLICT";
    private static final String STATUS_ADDED = "added";
    private static final String STATUS_EDITED = "edited";
    private static final String STATUS_DELETED = "deleted";

    private static NoteSynchronizer synchronizer;

    private ColorDao colorDao = new ColorDaoImpl();
    private File unsynchronizedNotesPath;
    private NoteService service;
    private UnsynchronizedNotes unsynchronizedNotes;

    public static NoteSynchronizer getInstance() {
        if (synchronizer == null)
            synchronizer = new NoteSynchronizer();
        return synchronizer;
    }

    public ColorRecord findNoteByServerId(int ownerId, int serverId) {
        ColorRecord[] records = colorDao.getColors(null, ownerId);
        for (ColorRecord record : records)
            if (record.getServerId() == serverId)
                return record;
        return null;
    }

    private NoteSynchronizer() {
        this.colorDao = new ColorDaoImpl();
        service = ServiceGenerator.createService(NoteService.class);
        unsynchronizedNotesPath = new File(
                MainApplication.getContext().getExternalFilesDir(null), "unsynchronized_notes.json");
        if (!unsynchronizedNotesPath.exists()) {
            try {
                unsynchronizedNotesPath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        readFromDisk();
    }

    public UnsynchronizedNotes getUnsynchronizedNotes(int ownerId) {
        return unsynchronizedNotes.extractUnsynchronizedNotesForOwner(ownerId);
    }

    public void add(ColorRecord record) {
        Log.d(TAG, "Add: " + record.toString());
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse<Integer> response = service.addNote(record.getOwnerId(), record).execute().body();
                if (response.getStatus().equals("ok")) {
                    record.setServerId(response.getData());
                    colorDao.saveColor(record);
                    removeIfExists(record);
                } else throw new RuntimeException();
            } catch (Exception e) {
                addToCache(record, STATUS_ADDED);
                e.printStackTrace();
            }
        } else {
            addToCache(record, STATUS_ADDED);
        }
    }

    public void save(ColorRecord record) {
        Log.d(TAG, "Save: " + record.toString());
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse response = service.saveNote(record.getOwnerId(), record.getServerId(), record).execute().body();
                if (response.getStatus().equals("ok")) {
                    removeIfExists(record);
                } else {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                addToCache(record, STATUS_EDITED);
                e.printStackTrace();
            }
        } else {
            addToCache(record, STATUS_EDITED);
        }
    }

    public void delete(ColorRecord record) {
        Log.d(TAG, "Delete: " + record.toString());
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse response = service.deleteNote(record.getOwnerId(), record.getServerId()).execute().body();
                if (response.getStatus().equals("ok")) {
                    removeIfExists(record);
                } else {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                addToCache(record, STATUS_DELETED);
                e.printStackTrace();
            }
        } else {
            addToCache(record, STATUS_DELETED);
        }
    }

    public void clearCache() {
        setUnsynchronizedNotes(null);
    }


    private void addToCache(ColorRecord record, String status) {
        Log.d(TAG, "Caching: " + status + " - " + record.toString());
        switch (status) {
            case STATUS_ADDED: {
                unsynchronizedNotes.added.put(record.getId(), record);
                break;
            }
            case STATUS_EDITED: {
                if (unsynchronizedNotes.added.containsKey(record.getId())) {
                    unsynchronizedNotes.added.put(record.getId(), record);
                } else unsynchronizedNotes.edited.put(record.getServerId(), record);
                break;
            }
            case STATUS_DELETED: {
                if (unsynchronizedNotes.added.containsKey(record.getId()))
                    unsynchronizedNotes.added.remove(record.getId());
                else if (unsynchronizedNotes.edited.containsKey(record.getServerId()))
                    unsynchronizedNotes.edited.remove(record.getServerId());
                else unsynchronizedNotes.deleted.put(record.getServerId(), record);
                break;
            }
        }
        writeToDisk();
        readFromDisk();
    }

    private void removeIfExists(ColorRecord note) {
        unsynchronizedNotes.added.remove(note.getId());
        unsynchronizedNotes.edited.remove(note.getServerId());
        unsynchronizedNotes.deleted.remove(note.getServerId());
    }

    void readFromDisk() {
        try (FileReader reader = new FileReader(unsynchronizedNotesPath)) {
            UnsynchronizedNotes newUnsynchronizedNotes =
                    SerializationUtils.GSON.fromJson(reader, UnsynchronizedNotes.class);
            this.unsynchronizedNotes = newUnsynchronizedNotes == null ? new UnsynchronizedNotes() : newUnsynchronizedNotes;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeToDisk() {
        try (FileWriter writer = new FileWriter(unsynchronizedNotesPath)) {
            SerializationUtils.GSON.toJson(unsynchronizedNotes, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UnsynchronizedNotes getUnsynchronizedNotes() {
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

    private void setUnsynchronizedNotes(UnsynchronizedNotes unsynchronizedNotes) {
        try (FileWriter writer = new FileWriter(unsynchronizedNotesPath)) {
            SerializationUtils.GSON.toJson(unsynchronizedNotes, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
