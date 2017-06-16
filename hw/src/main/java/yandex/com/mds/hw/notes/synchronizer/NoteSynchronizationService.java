package yandex.com.mds.hw.notes.synchronizer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.util.ArrayList;

import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.notes.synchronizer.conflicts.ConflictNotes;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;

public class NoteSynchronizationService extends IntentService {
    private static final String TAG = NoteSynchronizationService.class.getName();
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_URL = "URL";

    public static final String SYNC_COMPLETE_ACTION = "SYNC_COMPLETE";
    public static final String SYNC_CONFLICT_NOTES = "CONFLICT_NOTES";
    public static final String SYNC_ILLEGAL_FORMAT = "ILLEGAL_FORMAT";
    public static final String SYNC_ERROR_UNCLASSIFIED = "UNCLASSIFIED_ERROR";

    private NoteSynchronizer synchronizer;
    private int userId;

    public static void startNoteSynchronizer(Context context, int userId) {
        Intent intent = new Intent(context, NoteSynchronizationService.class);
        intent.putExtra(KEY_USER_ID, userId);
        context.startService(intent);
    }

    public static void startNoteSynchronizer(Context context, int userId, String url) {
        Intent intent = new Intent(context, NoteSynchronizationService.class);
        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra(KEY_URL, url);
        context.startService(intent);
    }

    public NoteSynchronizationService() {
        super(NoteSynchronizationService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        userId = intent.getExtras().getInt(KEY_USER_ID);
        NoteService service;
        if (intent.getExtras().getString(KEY_URL) == null)
            service = ServiceGenerator.createService(NoteService.class);
        else
            service = ServiceGenerator.createService(NoteService.class, intent.getExtras().getString(KEY_URL));
        synchronizer = new NoteSynchronizer(this, service, new DiskUnsynchronizedNotesManager());
        Intent syncCompleteAction = new Intent(SYNC_COMPLETE_ACTION);
        ArrayList<ConflictNotes> conflictNotes = null;
        try {
            conflictNotes = synchronizer.synchronize(userId);
            if (conflictNotes != null && !conflictNotes.isEmpty()) {
                syncCompleteAction.putParcelableArrayListExtra(SYNC_CONFLICT_NOTES, conflictNotes);
            }
        } catch (MalformedJsonException e) {
            syncCompleteAction.putExtra(SYNC_ILLEGAL_FORMAT, true);
        } catch (IOException e) {
            syncCompleteAction.putExtra(SYNC_ERROR_UNCLASSIFIED, true);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(syncCompleteAction));
    }
}
