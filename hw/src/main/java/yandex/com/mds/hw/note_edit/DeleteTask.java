package yandex.com.mds.hw.note_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;

public class DeleteTask extends AsyncTask<Integer, Void, Void> {
    private NoteDao noteDao;
    private NoteSynchronizer synchronizer;

    public DeleteTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(Integer[] params) {
        Note record = noteDao.getNote(params[0]);
        noteDao.deleteNote(params[0]);
        synchronizer.delete(record);
        return null;
    }
}
