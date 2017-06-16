package yandex.com.mds.hw.noteedit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import yandex.com.mds.hw.models.Note;

public class NotePagerAdapter extends FragmentStatePagerAdapter {
    private List<Note> notes;

    public NotePagerAdapter(FragmentManager fm, List<Note> notes) {
        super(fm);
        this.notes = notes;
    }

    @Override
    public Fragment getItem(int position) {
        Note note = notes.get(position);
        return NoteEditFragment.newInstance(
                note.getId(), note.getOwnerId(), String.valueOf(note.getId()));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return notes.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return notes.size();
    }
}