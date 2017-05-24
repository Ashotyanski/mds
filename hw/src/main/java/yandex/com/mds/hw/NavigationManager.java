package yandex.com.mds.hw;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.View;

import java.util.ArrayList;

import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.note_edit.NoteEditFragment;
import yandex.com.mds.hw.note_edit.NotePagerFragment;
import yandex.com.mds.hw.note_import_export.NoteImportExportFragment;
import yandex.com.mds.hw.notes.NotesFragment;

public class NavigationManager {
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

    public void showNotesImportExport() {
        NoteImportExportFragment fragment = new NoteImportExportFragment();
        openAsRoot(fragment);
    }

    private void open(Fragment fragment, View sharedView) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, fragment.getClass().getName());
        if (sharedView != null)
            transaction.addSharedElement(sharedView, ViewCompat.getTransitionName(sharedView));
//                    .setCustomAnimations(R.anim.slide_in_left,
//                            R.anim.slide_out_right,
//                            R.anim.slide_in_right,
//                            R.anim.slide_out_left)
        transaction.addToBackStack(fragment.getClass().getName()).commit();
    }

    private void openAsRoot(Fragment fragment) {
        popEveryFragment();
        open(fragment, null);
    }

    private void popEveryFragment() {
        // Clear all back stack.
        int backStackCount = mFragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            // Get the back stack fragment id.
            int backStackId = mFragmentManager.getBackStackEntryAt(i).getId();
            mFragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void navigateBack(Activity baseActivity) {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            // we can finish the base activity since we have no other fragments
            baseActivity.finish();
        } else {
            mFragmentManager.popBackStack();
        }
    }
}
