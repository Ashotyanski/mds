package yandex.com.mds.hw.colors.synchronizer;

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
    public static final String SYNC_CONFLICT_ACTION = "SYNC_CONFLICT";
    private static final String STATUS_ADDED = "added";
    private static final String STATUS_EDITED = "edited";
    private static final String STATUS_DELETED = "deleted";

    private static NoteSynchronizer synchronizer;

    private ColorDao colorDao = new ColorDaoImpl();
    private File unsynchronizedNotesPath;
    private NoteService service;

    public static NoteSynchronizer getInstance() {
        if (synchronizer == null)
            synchronizer = new NoteSynchronizer();
        return synchronizer;
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
    }

    public UnsynchronizedNotes getUnsynchronizedNotes(int ownerId) {
        UnsynchronizedNotes notes = getUnsynchronizedNotes();
        return notes.extractUnsynchronizedNotesForOwner(ownerId);
    }

    public void add(ColorRecord record) {
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse<Integer> response = service.addNote(record.getOwnerId(), record).execute().body();
                if (response.getStatus().equals("ok")) {
                    record.setServerId(response.getData());
                    colorDao.saveColor(record);
                } else throw new RuntimeException();
            } catch (Exception e) {
                addToCache(record, STATUS_ADDED);
                e.printStackTrace();
            }
        } else {
            addToCache(record, STATUS_ADDED);
        }
    }

    public void delete(ColorRecord record) {
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse response = service.deleteNote(record.getOwnerId(), record.getServerId()).execute().body();
                if (!response.getStatus().equals("ok")) {
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

    public void save(ColorRecord record) {
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse response = service.saveNote(record.getOwnerId(), record.getServerId(), record).execute().body();
                if (!response.getStatus().equals("ok")) {
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

    public void clearCache() {
        setUnsynchronizedNotes(null);
    }

    private void addToCache(ColorRecord record, String status) {
        UnsynchronizedNotes notes = getUnsynchronizedNotes();
        switch (status) {
            case STATUS_ADDED: {
                notes.added.put(record.getId(), record);
                break;
            }
            case STATUS_EDITED: {
                if (notes.added.containsKey(record.getId())) {
                    notes.added.put(record.getId(), record);
                } else notes.edited.put(record.getId(), record);
                break;
            }
            case STATUS_DELETED: {
                if (notes.added.containsKey(record.getId()))
                    notes.added.remove(record.getId());
                if (notes.edited.containsKey(record.getId()))
                    notes.edited.remove(record.getId());
                notes.deleted.put(record.getId(), record);
                break;
            }
        }
        setUnsynchronizedNotes(notes);
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
        FileWriter writer = null;
        try {
            writer = new FileWriter(unsynchronizedNotesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SerializationUtils.GSON.toJson(unsynchronizedNotes, writer);
    }
}
