package yandex.com.mds.hw.note_edit.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.notes.synchronizer.ServerNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.DiskUnsynchronizedNotesManager;
import yandex.com.mds.hw.notes.synchronizer.unsynchonizednotes.UnsynchronizedNotesManager;

public class AddTask extends AsyncTask<Note, Void, Void> {
    private NoteDao noteDao;
    private ServerNotesManager serverNotesManager;
    private UnsynchronizedNotesManager unsynchronizedNotesManager;

    public AddTask(NoteDao noteDao) {
        this.noteDao = noteDao;
        serverNotesManager = new ServerNotesManager();
        unsynchronizedNotesManager = new DiskUnsynchronizedNotesManager();
    }

    @Override
    protected Void doInBackground(Note[] params) {
        Note note = params[0];
        long newId = noteDao.addNote(note);
        note.setId((int) newId);
        try {
            int serverId = serverNotesManager.add(note);
            note.setServerId(serverId);
            noteDao.saveNote(note);
        } catch (IOException e) {
            e.printStackTrace();
            unsynchronizedNotesManager.putToAdded(note);
        }
        return null;
    }
}
