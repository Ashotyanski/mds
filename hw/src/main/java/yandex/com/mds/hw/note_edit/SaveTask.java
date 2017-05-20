package yandex.com.mds.hw.note_edit;

import android.os.AsyncTask;

import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;

public class SaveTask extends AsyncTask<Note, Void, Void> {
    private final NoteSynchronizer synchronizer;
    private NoteDao noteDao;

    public SaveTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(Note[] params) {
        noteDao.saveNote(params[0]);
        synchronizer.save(params[0]);
        return null;
    }
}
