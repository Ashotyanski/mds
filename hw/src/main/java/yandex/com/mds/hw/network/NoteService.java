package yandex.com.mds.hw.network;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import yandex.com.mds.hw.models.Note;

public interface NoteService {
    //    * GET /info — возвращает все хедеры запроса
    @GET("info")
    Call<NoteServiceResponse> getInfo();

    //* GET /user/%user_id%/notes — возвращает все заметки пользователя %user_id%
    @GET("user/{user_id}/notes")
    Call<NoteServiceResponse<List<Note>>> getNotes(@Path("user_id") int userId);

    @GET("user/{user_id}/notes")
    Call<ResponseBody> getNotesStr(@Path("user_id") int userId);

    //* GET /user/%user_id%/note/%note_id% — возвращает соответствующую заметку
    @GET("user/{user_id}/notes/{note_id}")
    Call<NoteServiceResponse<Note>> getNote(@Path("user_id") int userId, @Path("note_id") int noteId);

    //* POST /user/%user_id%/notes — создаёт новую заметку (в body запросе должен присутствовать JSON с создаваемой заметкой) и возвращает её ID
    @POST("user/{user_id}/notes")
    Call<NoteServiceResponse<Integer>> addNote(@Path("user_id") int userId, @Body Note note);

    //* POST /user/%user_id%/note/%note_id% — редактирует заметку (в body запросе должен присутствовать полный JSON с данными заметки)
    @POST("user/{user_id}/note/{note_id}")
    Call<NoteServiceResponse> saveNote(@Path("user_id") int userId, @Path("note_id") int noteId, @Body Note note);

    //* DELETE /user/%user_id%/note/%note_id% — удаляет заметку
    @DELETE("user/{user_id}/note/{note_id}")
    Call<NoteServiceResponse> deleteNote(@Path("user_id") int userId, @Path("note_id") int noteId);
}
