package yandex.com.mds.hw.note_edit.tasks;

import android.os.AsyncTask;

import yandex.com.mds.hw.notes.synchronizer.NoteSynchronizer;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;

public class AddTask extends AsyncTask<Note, Void, Void> {
    private NoteDao noteDao;
    private NoteSynchronizer synchronizer;

    public AddTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Override
    protected Void doInBackground(Note[] params) {
        Note record = params[0];
        long newId = noteDao.addNote(record);
        record.setId((int) newId);
        synchronizer.add(record);
        return null;
    }
}
