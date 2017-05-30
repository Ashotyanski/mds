package yandex.com.mds.hw.navigation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.transition.Fade;
import android.view.View;

import java.util.ArrayList;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.note_edit.NoteEditFragment;
import yandex.com.mds.hw.note_edit.NotePagerFragment;
import yandex.com.mds.hw.note_import_export.NoteImportExportFragment;
import yandex.com.mds.hw.notes.NotesFragment;

public class NavigationManager {
    private static final String TAG = NavigationManager.class.getName();
    private FragmentManager mFragmentManager;

    public NavigationManager(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            showNotes();
        }
    }

    public void showNotes() {
        NotesFragment fragment = new NotesFragment();
        openAsRoot(fragment);
    }

    public void showNotePager(int notePosition, ArrayList<Note> notes, View sharedView) {
        NotePagerFragment fragment = NotePagerFragment.newInstance(notePosition, notes, ViewCompat.getTransitionName(sharedView));
        open(fragment, sharedView);
    }

    public void showNoteAdd(int noteId, int userId) {
        NoteEditFragment fragment = NoteEditFragment.newInstance(noteId, userId, "");
        open(fragment, null);
    }

    public void showNoteEdit(int noteId, int userId, View sharedView) {
        NoteEditFragment fragment = NoteEditFragment.newInstance(noteId, userId, ViewCompat.getTransitionName(sharedView));
        open(fragment, sharedView);
    }

    public void showNotesImportExport() {
        NoteImportExportFragment fragment = new NoteImportExportFragment();
        openAsRoot(fragment);
    }

    private void open(Fragment fragment, View sharedView) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, fragment.getClass().getName());
        if (sharedView != null) {
            transaction.addSharedElement(sharedView, ViewCompat.getTransitionName(sharedView));
            mFragmentManager.getFragments().get(0)
                    .setEnterTransition(new Fade(Fade.IN));
            mFragmentManager.getFragments().get(0)
                    .setExitTransition(new Fade(Fade.OUT));
        }
        transaction.addToBackStack(fragment.getClass().getName()).commit();
    }

    private void openAsRoot(Fragment fragment) {
        popEveryFragment();
        open(fragment, null);
    }

    private void popEveryFragment() {
        int backStackCount = mFragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            int backStackId = mFragmentManager.getBackStackEntryAt(i).getId();
            mFragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void navigateBack(Activity baseActivity) {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            baseActivity.finish();
        } else {
            mFragmentManager.popBackStack();
        }
    }
}
