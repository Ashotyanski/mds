package yandex.com.mds.hw.notes.synchronizer;

import android.util.Log;

import java.io.IOException;
import java.net.UnknownServiceException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;

public class ServerNotesManager {
    private static final String TAG = ServerNotesManager.class.getName();

    private NoteService service;

    public ServerNotesManager() {
        service = ServiceGenerator.createService(NoteService.class);
    }

    public ServerNotesManager(NoteService service) {
        this.service = service;
    }

    public Integer add(Note record) throws IOException {
        Log.d(TAG, "Add: " + record.toString());
        NoteServiceResponse<Integer> response = service.addNote(record.getOwnerId(), record).execute().body();
        if (response == null || !response.getStatus().equals("ok"))
            throw new UnknownServiceException();
        else
            return response.getData();
    }

    public void addAsync(final Note note, final ActionCallback<Integer> callback) {
        Log.d(TAG, "Add async: " + note.toString());
        service.addNote(note.getOwnerId(), note).enqueue(new Callback<NoteServiceResponse<Integer>>() {
            @Override
            public void onResponse(Call<NoteServiceResponse<Integer>> call, Response<NoteServiceResponse<Integer>> response) {
                try {
                    if (response.body() != null && response.body().getStatus().equals("ok")) {
                        callback.onSuccess(response.body().getData());
                    } else {
                        throw new UnknownServiceException();
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Call<NoteServiceResponse<Integer>> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    public void save(Note record) throws IOException {
        Log.d(TAG, "Save: " + record.toString());
        NoteServiceResponse response = service.saveNote(record.getOwnerId(), record.getServerId(), record).execute().body();
        if (response == null || !response.getStatus().equals("ok")) {
            throw new UnknownServiceException();
        }
    }

    public void saveAsync(Note record, final ActionCallback<Void> callback) {
        Log.d(TAG, "Save async: " + record.toString());
        service.saveNote(record.getOwnerId(), record.getServerId(), record).enqueue(new Callback<NoteServiceResponse>() {
            @Override
            public void onResponse(Call<NoteServiceResponse> call, Response<NoteServiceResponse> response) {
                try {
                    if (response.body() != null && response.body().getStatus().equals("ok"))
                        callback.onSuccess(null);
                    else
                        throw new UnknownServiceException();
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Call<NoteServiceResponse> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    public void delete(Note record) throws IOException {
        Log.d(TAG, "Delete: " + record);
        NoteServiceResponse response = service.deleteNote(record.getOwnerId(), record.getServerId()).execute().body();
        if (response == null || !response.getStatus().equals("ok")) {
            throw new UnknownServiceException();
        }
    }

    public void deleteAsync(Note record, final ActionCallback<Void> callback) {
        Log.d(TAG, "Delete async: " + record.toString());
        service.deleteNote(record.getOwnerId(), record.getServerId()).enqueue(new Callback<NoteServiceResponse>() {
            @Override
            public void onResponse(Call<NoteServiceResponse> call, Response<NoteServiceResponse> response) {
                if (response.body() != null && response.body().getStatus().equals("ok"))
                    callback.onSuccess(null);
                else
                    callback.onFailure(new Exception());
            }

            @Override
            public void onFailure(Call<NoteServiceResponse> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    public NoteService getService() {
        return service;
    }

    public void setService(NoteService service) {
        this.service = service;
    }

    public interface ActionCallback<T> {
        void onSuccess(T response);

        void onFailure(Exception e);
    }
}
