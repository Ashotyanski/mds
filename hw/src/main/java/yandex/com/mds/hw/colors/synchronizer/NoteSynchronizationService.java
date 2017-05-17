package yandex.com.mds.hw.colors.synchronizer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        List<ColorRecord> remoteNotes = null;
        try {
            NoteServiceResponse<List<ColorRecord>> response = noteService.getNotes(userId).execute().body();
            if (response.getStatus().equals("ok"))
                remoteNotes = response.getData();
            Log.d(TAG, "onHandleIntent: " + response);
        } catch (Exception e) {
            Log.d(TAG, "onHandleIntent: Exception");
            e.printStackTrace();
            stopSelf();
        }
        Set<ColorRecord> remoteNoteSet = new HashSet<>(remoteNotes);
        UnsynchronizedNotes unsynchronizedNotes = synchronizer.getUnsynchronizedNotes(userId);

        Map<ColorRecord, ColorRecord> conflictNotes = new HashMap<>();
        List<ColorRecord> readyToSendNotes = new ArrayList<>();
        List<ColorRecord> readyToImportNotes = new ArrayList<>();
        List<ColorRecord> readyToSaveRemoteNotes = new ArrayList<>();
        List<Integer> readyToDeleteNotes = new ArrayList<>();


        for (Map.Entry<Integer, ColorRecord> entry : unsynchronizedNotes.deleted.entrySet()) {
            for (Iterator<ColorRecord> it = remoteNotes.iterator(); it.hasNext(); ) {
                if (it.next().getServerId() == entry.getKey()) {
                    readyToDeleteNotes.add(entry.getKey());
                    it.remove();
                    break;
                }
            }
        }
        unsynchronizedNotes.deleted.clear();
        for (Map.Entry<Integer, ColorRecord> entry : unsynchronizedNotes.edited.entrySet()) {
            for (ColorRecord remoteNote : remoteNotes) {
                if (remoteNote.getServerId() == entry.getKey()) {
                    if (remoteNote.getLastModificationDate().getTime() < entry.getValue().getLastModificationDate().getTime())
                        readyToSaveRemoteNotes.add(entry.getValue());
                    else if (remoteNote.getLastModificationDate().getTime() > entry.getValue().getLastModificationDate().getTime()) {
                        conflictNotes.put(entry.getValue(), remoteNote);
                    } else {
                        //ok
                    }
                    break;
                }
            }
        }
        if (!conflictNotes.isEmpty())
            sendBroadcast(new Intent(NoteSynchronizer.SYNC_CONFLICT_ACTION));

        for (Map.Entry<Integer, ColorRecord> entry : unsynchronizedNotes.added.entrySet()) {
            readyToSendNotes.add(entry.getValue());
        }


        /*
                noteService.getNotes(currentUserId).enqueue(new Callback<NoteServiceResponse<List<ColorRecord>>>() {
            @Override
            public void onResponse(Call<NoteServiceResponse<List<ColorRecord>>> call, Response<NoteServiceResponse<List<ColorRecord>>> response) {
                try {
                    Log.d(TAG, "onResponse: " + response.body().getStatus());
                    Log.d(TAG, "onResponse: " + response.body().getData());
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: Exception");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<NoteServiceResponse<List<ColorRecord>>> call, Throwable t) {
                Log.d(TAG, "onFailure: failed");
                t.printStackTrace();
            }
        });
//        noteService.getNotesStr(currentUserId).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                try {
//                    Log.d(TAG, "Request: " + call.request().toString());
//                    Log.d(TAG, "Headers: " + response.body().string());
//                    Log.d(TAG, String.valueOf((SerializationUtils.GSON.fromJson(response.body().string(), NoteServiceResponse.class))));
//                } catch (Exception e) {
//                    Log.d(TAG, "Headers: Exception");
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.d(TAG, "Headers fail: " + t.getMessage());
//            }
//        });
         */
    }
}
