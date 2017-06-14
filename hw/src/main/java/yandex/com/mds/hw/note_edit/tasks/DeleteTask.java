package yandex.com.mds.hw.note_edit.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import yandex.com.mds.hw.notes.synchronizer.ServerNotesManager;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;

public class DeleteTask extends AsyncTask<Integer, Void, Void> {
    private NoteDao noteDao;
    private ServerNotesManager serverNotesManager;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;

    public DeleteTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        serverNotesManager = new ServerNotesManager();
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
    }

    @Override
    protected Void doInBackground(Integer[] params) {
        Note record = noteDao.getNote(params[0]);
        noteDao.deleteNote(params[0]);
        try {
            serverNotesManager.delete(record);
        } catch (IOException e) {
            e.printStackTrace();
            unsynchronizedNotesManager.putToEdited(record);
        }
        return null;
    }
}
