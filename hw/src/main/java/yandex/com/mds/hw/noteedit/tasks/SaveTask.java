package yandex.com.mds.hw.noteedit.tasks;

import android.os.AsyncTask;

import yandex.com.mds.hw.notes.synchronizer.ServerNotesManager;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;

public class SaveTask extends AsyncTask<Note, Void, Void> {
    private NoteDao noteDao;
    private ServerNotesManager serverNotesManager;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;

    public SaveTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        serverNotesManager = new ServerNotesManager();
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
    }

    @Override
    protected Void doInBackground(Note[] params) {
        Note note = params[0];
        noteDao.saveNote(note);
        try {
            serverNotesManager.save(note);
            unsynchronizedNotesManager.remove(note);
        } catch (Exception e) {
            unsynchronizedNotesManager.putToEdited(note);
        }
        return null;
    }
}
