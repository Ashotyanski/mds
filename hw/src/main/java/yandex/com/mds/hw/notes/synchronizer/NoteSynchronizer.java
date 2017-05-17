package yandex.com.mds.hw.notes.synchronizer;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yandex.com.mds.hw.MainApplication;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.utils.NetworkUtils;
import yandex.com.mds.hw.utils.SerializationUtils;

public class NoteSynchronizer {
    private static final String TAG = NoteSynchronizer.class.getName();
    public static final String SYNC_COMPLETE_ACTION = "SYNC_COMPLETE";
    private static final String STATUS_ADDED = "added";
    private static final String STATUS_EDITED = "edited";
    private static final String STATUS_DELETED = "deleted";

    private static NoteSynchronizer synchronizer;

    private NoteDao noteDao = new NoteDaoImpl();
    private File unsynchronizedNotesPath;
    private NoteService service;
    private UnsynchronizedNotes unsynchronizedNotes;

    public static NoteSynchronizer getInstance() {
        if (synchronizer == null)
            synchronizer = new NoteSynchronizer();
        return synchronizer;
    }

    private NoteSynchronizer() {
        this.noteDao = new NoteDaoImpl();
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

    public void addAsync(final Note record) {
        Log.d(TAG, "Add async: " + record.toString());
        if (NetworkUtils.isConnected()) {
            service.addNote(record.getOwnerId(), record).enqueue(new Callback<NoteServiceResponse<Integer>>() {
                @Override
                public void onResponse(Call<NoteServiceResponse<Integer>> call, Response<NoteServiceResponse<Integer>> response) {
                    if (response.body() != null && response.body().getStatus().equals("ok")) {
                        record.setServerId(response.body().getData());
                        noteDao.saveNote(record);
                        removeIfExists(record);
                    } else
                        addToCache(record, STATUS_ADDED);
                }

                @Override
                public void onFailure(Call<NoteServiceResponse<Integer>> call, Throwable t) {
                    addToCache(record, STATUS_ADDED);
                }
            });
        } else {
            addToCache(record, STATUS_ADDED);
        }
    }

    public void add(Note record) {
        Log.d(TAG, "Add: " + record.toString());
        if (NetworkUtils.isConnected()) {
            try {
                NoteServiceResponse<Integer> response = service.addNote(record.getOwnerId(), record).execute().body();
                if (response.getStatus().equals("ok")) {
                    record.setServerId(response.getData());
                    noteDao.saveNote(record);
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

    public void saveAsync(final Note record) {
        Log.d(TAG, "Save async: " + record.toString());
        if (NetworkUtils.isConnected()) {
            service.saveNote(record.getOwnerId(), record.getServerId(), record).enqueue(new Callback<NoteServiceResponse>() {
                @Override
                public void onResponse(Call<NoteServiceResponse> call, Response<NoteServiceResponse> response) {
                    if (response.body() != null && response.body().getStatus().equals("ok"))
                        removeIfExists(record);
                    else
                        addToCache(record, STATUS_EDITED);
                }

                @Override
                public void onFailure(Call<NoteServiceResponse> call, Throwable t) {
                    addToCache(record, STATUS_EDITED);
                }
            });
        } else {
            addToCache(record, STATUS_EDITED);
        }
    }

    public void save(Note record) {
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

    public void deleteAsync(final Note record) {
        Log.d(TAG, "Delete async: " + record.toString());
        if (NetworkUtils.isConnected()) {
            service.deleteNote(record.getOwnerId(), record.getServerId()).enqueue(new Callback<NoteServiceResponse>() {
                @Override
                public void onResponse(Call<NoteServiceResponse> call, Response<NoteServiceResponse> response) {
                    if (response.body() != null && response.body().getStatus().equals("ok"))
                        removeIfExists(record);
                    else
                        addToCache(record, STATUS_DELETED);
                }

                @Override
                public void onFailure(Call<NoteServiceResponse> call, Throwable t) {
                    addToCache(record, STATUS_DELETED);
                }
            });
        } else {
            addToCache(record, STATUS_DELETED);
        }
    }

    public void delete(Note record) {
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

    public Note findNoteByServerId(int ownerId, int serverId) {
        List<Note> records = noteDao.getNotes(null, ownerId);
        for (Note record : records)
            if (record.getServerId() == serverId)
                return record;
        return null;
    }

    public void clearCache() {
        setUnsynchronizedNotes(null);
    }


    private void addToCache(Note record, String status) {
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

    private void removeIfExists(Note note) {
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
