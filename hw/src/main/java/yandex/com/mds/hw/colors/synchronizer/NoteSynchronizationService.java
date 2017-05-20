package yandex.com.mds.hw.colors.synchronizer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.utils.TimeUtils;

public class NoteSynchronizationService extends IntentService {
    private static final String TAG = NoteSynchronizationService.class.getName();
    private static final String KEY_USER_ID = "USER_ID";

    public static final String KEY_CONFLICT_NOTES = "CONFLICT_NOTES";
    public static final String KEY_ILLEGAL_FORMAT = "ILLEGAL_FORMAT";

    private NoteService noteService;
    private NoteSynchronizer synchronizer;
    private ColorDao colorDao;
    private int userId;

    public static void startNoteSynchronizer(Context context, int userId) {
        Intent intent = new Intent(context, NoteSynchronizationService.class);
        intent.putExtra(KEY_USER_ID, userId);
        context.startService(intent);
    }

    public NoteSynchronizationService() {
        super(NoteSynchronizationService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        userId = intent.getExtras().getInt(KEY_USER_ID);
        colorDao = new ColorDaoImpl();
        noteService = ServiceGenerator.createService(NoteService.class);
        synchronizer = NoteSynchronizer.getInstance();

        UnsynchronizedNotes unsynchronizedNotes = synchronizer.getUnsynchronizedNotes(userId);
        List<ColorRecord> remoteNotes = null;
        try {
            NoteServiceResponse<List<ColorRecord>> response = noteService.getNotes(userId).execute().body();
            if (response.getStatus().equals("ok"))
                remoteNotes = response.getData();
            Log.d(TAG, "onHandleIntent: " + response);
        } catch (Exception e) {
            Log.d(TAG, "Exception while parsing response");
            e.printStackTrace();
            Intent conflictNotesIntent = new Intent(NoteSynchronizer.SYNC_COMPLETE_ACTION);
            conflictNotesIntent.putExtra(KEY_ILLEGAL_FORMAT, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conflictNotesIntent));
            return;
        }
        ArrayList<ConflictNotes> conflictNotes = new ArrayList<>();
        SparseArray<ColorRecord> diff = new SparseArray<>();
        for (ColorRecord record : colorDao.getColors(null, userId))
            if (record.getServerId() > 0)
                diff.put(record.getServerId(), record);

        for (Iterator<ColorRecord> it = remoteNotes.iterator(); it.hasNext(); ) {
            ColorRecord remoteNote = it.next();
            if (unsynchronizedNotes.deleted.containsKey(remoteNote.getId())) {
                ColorRecord note = unsynchronizedNotes.deleted.get(remoteNote.getId());
                Log.d(TAG, "Deleting remote note: " + note);
                synchronizer.delete(note);
                unsynchronizedNotes.deleted.remove(remoteNote.getId());
                break;
            } else if (unsynchronizedNotes.edited.containsKey(remoteNote.getId())) {
                ColorRecord note = unsynchronizedNotes.edited.get(remoteNote.getId());
                if (remoteNote.getLastModificationDate().getTime() < note.getLastModificationDate().getTime()) {
                    Log.d(TAG, "Overwriting remote note: " + note.toString());
                    synchronizer.save(note);
                } else if (remoteNote.getLastModificationDate().getTime() > note.getLastModificationDate().getTime()) {
                    Log.d(TAG, "Conflicting notes:\n" + note.toString() + "\n" + remoteNote.toString());
                    conflictNotes.add(new ConflictNotes(note, remoteNote));
                } else {
                    Log.d(TAG, "Already deleted");
                    unsynchronizedNotes.edited.remove(remoteNote.getId());
                }
            } else if (synchronizer.findNoteByServerId(userId, remoteNote.getId()) == null) {
                remoteNote.setOwnerId(userId);
                remoteNote.setServerId(remoteNote.getId());
                Log.d(TAG, "Adding new note: " + remoteNote.toString());
                colorDao.addColor(remoteNote);
            } else {
                ColorRecord localPairNote = diff.get(remoteNote.getId());
                if (isSame(localPairNote, remoteNote))
                    Log.d(TAG, "Note is synchronized already");
                else {
                    Log.d(TAG, "Changed on remote, conflict");
                    conflictNotes.add(new ConflictNotes(localPairNote, remoteNote));
                }
            }
            diff.remove(remoteNote.getId());
        }

        // send added notes
        for (ColorRecord note : unsynchronizedNotes.added.values()) {
            Log.d(TAG, "Sending note: " + note.toString());
            synchronizer.add(note);
        }

        // locally present but not represented at remote - we consider them deleted at remote
        // and thereby conflicting (these include both edited and already synchronized)
        for (int i = 0; i < diff.size(); i++) {
            conflictNotes.add(new ConflictNotes(diff.valueAt(i), null));
        }

        Intent conflictNotesIntent = new Intent(NoteSynchronizer.SYNC_COMPLETE_ACTION);
        if (!conflictNotes.isEmpty()) {
            conflictNotesIntent.putParcelableArrayListExtra(KEY_CONFLICT_NOTES, conflictNotes);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conflictNotesIntent));
    }

    private boolean isSame(ColorRecord local, ColorRecord remote) {
        try {
            return (local.getColor() == remote.getColor() &&
                    local.getTitle().equals(remote.getTitle()) &&
                    local.getDescription().equals(remote.getDescription()) &&
                    TimeUtils.trimMilliseconds(local.getCreationDate()).getTime()
                            == TimeUtils.trimMilliseconds(remote.getCreationDate()).getTime() &&
                    TimeUtils.trimMilliseconds(local.getLastModificationDate()).getTime()
                            == TimeUtils.trimMilliseconds(remote.getLastModificationDate()).getTime() &&
                    local.getImageUrl().equals(remote.getImageUrl()));
        } catch (NullPointerException e) {
            return false;
        }
    }
}
