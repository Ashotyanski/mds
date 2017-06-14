package yandex.com.mds.hw.notes.synchronizer;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.notes.synchronizer.conflicts.ConflictNotes;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotes;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;
import yandex.com.mds.hw.utils.TimeUtils;

public class NoteSynchronizer {
    private static final String TAG = NoteSynchronizer.class.getName();
    private NoteDao noteDao;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;
    private ServerNotesManager serverNotesManager;
    private NoteService noteService;

    public NoteSynchronizer(Context context, NoteService service, UnsynchronizedNotesManager unsynchronizedNotesManager) {
        this.unsynchronizedNotesManager = unsynchronizedNotesManager;
        this.noteService = service;
        serverNotesManager = new ServerNotesManager(noteService);
        noteDao = new NoteDaoImpl();
    }

    public ArrayList<ConflictNotes> synchronize(int userId) throws IOException {
        UnsynchronizedNotes unsynchronizedNotes = unsynchronizedNotesManager.getUnsynchronizedNotes(userId);
        List<Note> remoteNotes = null;
        try {
            NoteServiceResponse<List<Note>> response = noteService.getNotes(userId).execute().body();
            if (response.getStatus().equals("ok"))
                remoteNotes = response.getData();
        } catch (Exception e) {
            Log.d(TAG, "Exception while parsing response");
            e.printStackTrace();
            throw e;
        }
        ArrayList<ConflictNotes> conflictNotes = new ArrayList<>();
        SparseArray<Note> diff = new SparseArray<>();
        for (Note record : noteDao.getNotes(null, userId))
            if (record.getServerId() > 0)
                diff.put(record.getServerId(), record);
        Log.d(TAG, "synchronize: diff=" + diff.toString());
        Log.d(TAG, "synchronize: remote=" + remoteNotes.toString());

        //iterate through all remote notes
        for (Iterator<Note> it = remoteNotes.iterator(); it.hasNext(); ) {
            Note remoteNote = it.next();
            if (unsynchronizedNotes.getDeleted().containsKey(remoteNote.getId())) {
                //if is deleted locally then delete the remote one too
                Note note = unsynchronizedNotes.getDeleted().get(remoteNote.getId());
                Log.d(TAG, "Deleting remote note: " + note);
                try {
                    serverNotesManager.delete(note);
                    unsynchronizedNotesManager.remove(note);
                } catch (IOException e) {
                    Log.d(TAG, "Exception occurred");
                    e.printStackTrace();
                }
                continue;
            } else if (unsynchronizedNotes.getEdited().containsKey(remoteNote.getId())) {
                //if is edited locally
                Note note = unsynchronizedNotes.getEdited().get(remoteNote.getId());
                if (remoteNote.getLastModificationDate().getTime() < note.getLastModificationDate().getTime()) {
                    //and last edit is at a later time than last remote edit, then overwrite
                    Log.d(TAG, "Overwriting remote note: " + note.toString());
                    try {
                        serverNotesManager.save(note);
                        unsynchronizedNotesManager.remove(note);
                    } catch (IOException e) {
                        Log.d(TAG, "Exception occurred");
                        e.printStackTrace();
                    }
                } else if (remoteNote.getLastModificationDate().getTime() > note.getLastModificationDate().getTime()) {
                    //and last edit is at an earlier time than last remote edit, then it is a conflict
                    Log.d(TAG, "Conflicting notes:\n" + note.toString() + "\n" + remoteNote.toString());
                    conflictNotes.add(new ConflictNotes(note, remoteNote));
                }
            } else if (findNoteByServerId(userId, remoteNote.getId()) == null) {
                //if exists remotely but does not locally, then we add it
                remoteNote.setOwnerId(userId);
                remoteNote.setServerId(remoteNote.getId());
                Log.d(TAG, "Adding new note: " + remoteNote.toString());
                noteDao.addNote(remoteNote);
            } else {
                //both exist, we make sure they match
                Note localPairNote = diff.get(remoteNote.getId());
                if (isSame(localPairNote, remoteNote))
                    Log.d(TAG, "Note is synchronized already");
                else {
                    Log.d(TAG, "Difference, conflict");
                    conflictNotes.add(new ConflictNotes(localPairNote, remoteNote));
                }
            }
            diff.remove(remoteNote.getId());
        }

        // send added notes
        for (Note note : unsynchronizedNotes.getAdded().values()) {
            Log.d(TAG, "Sending note: " + note.toString());
            try {
                int serverId = serverNotesManager.add(note);
                note.setServerId(serverId);
                noteDao.saveNote(note);
                unsynchronizedNotesManager.remove(note);
            } catch (IOException e) {
                Log.d(TAG, "Exception occurred");
                throw e;
            }
        }

        // locally present but not represented at remote - we consider them deleted at remote
        // and thereby conflicting (these include both edited and already synchronized)
        for (int i = 0; i < diff.size(); i++) {
            conflictNotes.add(new ConflictNotes(diff.valueAt(i), null));
        }
        return conflictNotes;
    }

    private Note findNoteByServerId(int ownerId, int serverId) {
        List<Note> records = noteDao.getNotes(null, ownerId);
        for (Note record : records)
            if (record.getServerId() == serverId)
                return record;
        return null;
    }

    static boolean isSame(Note local, Note remote) {
        //color
        if (local.getColor() != remote.getColor())
            return false;
        //title
        local.setTitle(local.getTitle() == null ? "" : local.getTitle());
        remote.setTitle(remote.getTitle() == null ? "" : remote.getTitle());
        if (!local.getTitle().equals(remote.getTitle()))
            return false;
        //description
        local.setDescription(local.getDescription() == null ? "" : local.getDescription());
        remote.setDescription(remote.getDescription() == null ? "" : remote.getDescription());
        if (!local.getDescription().equals(remote.getDescription()))
            return false;
        //image
        local.setImageUrl(local.getImageUrl() == null ? "" : local.getImageUrl());
        remote.setImageUrl(remote.getImageUrl() == null ? "" : remote.getImageUrl());
        if (!local.getImageUrl().equals(remote.getImageUrl()))
            return false;
        //creation date
        if (local.getCreationDate() == null ^ remote.getCreationDate() == null)
            return false;
        if ((local.getCreationDate() != null && remote.getCreationDate() != null) &&
                TimeUtils.trimMilliseconds(local.getCreationDate()).getTime()
                        != TimeUtils.trimMilliseconds(remote.getCreationDate()).getTime())
            return false;
        //last modification date
        if (local.getLastModificationDate() == null ^ remote.getLastModificationDate() == null)
            return false;
        if ((local.getLastModificationDate() != null && remote.getLastModificationDate() != null) &&
                TimeUtils.trimMilliseconds(local.getLastModificationDate()).getTime()
                        != TimeUtils.trimMilliseconds(remote.getLastModificationDate()).getTime())
            return false;
        return true;
    }
}
