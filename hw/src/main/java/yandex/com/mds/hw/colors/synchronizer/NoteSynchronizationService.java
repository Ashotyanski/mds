package yandex.com.mds.hw.colors.synchronizer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;

public class NoteSynchronizationService extends IntentService {
    private static final String TAG = NoteSynchronizationService.class.getName();
    private static final String ACTION_SYNC_NOTES = "com.yandex.mds.SYNC_NOTES";
    private static final String KEY_USER_ID = "USER_ID";
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
            Log.d(TAG, "onHandleIntent: Exception");
            e.printStackTrace();
            stopForeground(true);
            stopSelf();
        }
        Map<ColorRecord, ColorRecord> conflictNotes = new HashMap<>();

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
                    conflictNotes.put(note, remoteNote);
                } else {
                    Log.d(TAG, "Already deleted");
                    unsynchronizedNotes.edited.remove(remoteNote.getId());
                }
            } else if (synchronizer.findNoteByServerId(userId, remoteNote.getId()) == null) {
                remoteNote.setOwnerId(userId);
                remoteNote.setServerId(remoteNote.getId());
                Log.d(TAG, "Adding new color: " + remoteNote.toString());
                colorDao.addColor(remoteNote);
            } else {
                Log.d(TAG, "Color is synchronized already");
            }
        }
        if (!conflictNotes.isEmpty())
            sendBroadcast(new Intent(NoteSynchronizer.SYNC_CONFLICT_ACTION));

        for (ColorRecord note : unsynchronizedNotes.added.values()) {
            Log.d(TAG, "Sending color: " + note.toString());
            synchronizer.add(note);
        }
    }
}
