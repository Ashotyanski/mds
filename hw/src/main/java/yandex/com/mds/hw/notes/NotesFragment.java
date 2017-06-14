package yandex.com.mds.hw.notes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.note_import_export.NoteImporterExporter;
import yandex.com.mds.hw.notes.query.Query;
import yandex.com.mds.hw.notes.query.presenters.QueryPresenter;
import yandex.com.mds.hw.notes.synchronizer.conflicts.ConflictNotes;
import yandex.com.mds.hw.notes.synchronizer.conflicts.SyncConflictFragment;

import static android.content.Context.MODE_PRIVATE;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_COMPLETE_ACTION;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_CONFLICT_NOTES;
import static yandex.com.mds.hw.notes.synchronizer.NoteSynchronizationService.SYNC_ILLEGAL_FORMAT;

public class NotesFragment extends Fragment {
    private static final String TAG = NotesFragment.class.getName();
    public static final String PREFERENCES_USER = "USER";
    private static final String QUERY_BUNDLE_KEY = "query";
    private static final String USER_ID_BUNDLE_KEY = "user_id";

    private RecyclerView list;
    private QueryPresenter queryPresenter;
    private NoteLoader noteLoader;

    private NoteDao noteDao = new NoteDaoImpl();
    private BroadcastReceiver syncCompleteReceiver;
    private BroadcastReceiver importReceiver;
    private View view;

    public NotesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.content_notes, null);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //&& !drawer.isDrawerOpen(GravityCompat.START)
                if (queryPresenter.isShown() && !queryPresenter.isTouched(event.getX(), event.getY())) {
                    queryPresenter.close();
                    return true;
                }
                return v.onTouchEvent(event);
            }
        });
        ActionBar toolbar = ((MainActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(R.string.title_notes);

        list = (RecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                layoutManager.getOrientation());
        list.addItemDecoration(dividerItemDecoration);
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(((NotesRecyclerViewAdapter) recyclerView.getAdapter()).getNotes(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(list);
        syncCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                final Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.getBoolean(SYNC_ILLEGAL_FORMAT)) {
                        Snackbar.make(list, R.string.sync_format_illegal, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (extras.<ConflictNotes>getParcelableArrayList(SYNC_CONFLICT_NOTES) != null) {
                        Snackbar snack = Snackbar.make(list, R.string.sync_conflict_message, Snackbar.LENGTH_INDEFINITE);
                        snack.setAction(R.string.sync_conflict_action_resolve, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .add(SyncConflictFragment.newInstance(
                                                        extras.<ConflictNotes>getParcelableArrayList(SYNC_CONFLICT_NOTES)), "CONFLICT"
                                                )
                                                .commit();
                                    }
                                }
                        );
                        snack.show();
                    }
                }
                loadNotes();
            }
        };
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote(getCurrentUserId(getContext()));
            }
        });

        queryPresenter = new QueryPresenter(getContext(), new QueryPresenter.OnApplyQueryListener() {
            @Override
            public void onApply(Query query) {
                loadNotes(query, getCurrentUserId(getContext()));
            }
        }, (LinearLayout) view.findViewById(R.id.query));
        NotesRecyclerViewAdapter adapter = new NotesRecyclerViewAdapter(
                noteDao.getNotes(queryPresenter.getQuery(), getCurrentUserId(getContext())));
        adapter.setOnClickListener(new NotesRecyclerViewAdapter.OnNoteSelectedListener() {
            @Override
            public void onNoteSelected(NotesRecyclerViewAdapter.ViewHolder holder, Note note) {
                View sharedView = holder.colorView;
                editNote(holder.getAdapterPosition(), ((NotesRecyclerViewAdapter) list.getAdapter()).getNotes(), sharedView);
            }
        });
        list.setAdapter(adapter);

        importReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadNotes();
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(syncCompleteReceiver, new IntentFilter(SYNC_COMPLETE_ACTION));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(importReceiver, NoteImporterExporter.importIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(syncCompleteReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(importReceiver);
    }

    private void loadNotes() {
        loadNotes(queryPresenter.getQuery(), getCurrentUserId(getContext()));
    }

    private void loadNotes(Query query, int userId) {
        if (noteLoader == null) noteLoader = new NoteLoader();
        if (getActivity().getSupportLoaderManager().getLoader(1) == null)
            getActivity().getSupportLoaderManager().initLoader(1, getColorLoaderBundle(query, userId), noteLoader).forceLoad();
        else
            getActivity().getSupportLoaderManager().restartLoader(1, getColorLoaderBundle(query, userId), noteLoader).forceLoad();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        queryPresenter.close();
    }

    private Bundle getColorLoaderBundle(Query query, int userId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(QUERY_BUNDLE_KEY, query);
        bundle.putInt(USER_ID_BUNDLE_KEY, userId);
        return bundle;
    }

    public static int getCurrentUserId(Context context) {
        return context.getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);
    }

    public static void setCurrentUserId(Context context, int id) {
        context.getSharedPreferences("USER", MODE_PRIVATE).edit().putInt("USER_ID", id).apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                queryPresenter.toggle();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    private void editNote(int noteId, List<Note> notes, View sharedView) {
        ((MainActivity) getActivity()).getNavigationManager().showNotePager(noteId, (ArrayList<Note>) notes, sharedView);
    }

    private void addNote(int userId) {
        ((MainActivity) getActivity()).getNavigationManager().showNoteAdd(userId);
    }

    private class NoteLoader implements LoaderManager.LoaderCallbacks<List<Note>> {
        @Override
        public Loader<List<Note>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Note>>(getContext()) {
                @Override
                public List<Note> loadInBackground() {
                    Query query = args.getParcelable(QUERY_BUNDLE_KEY);
                    int userId = args.getInt(USER_ID_BUNDLE_KEY);
                    return noteDao.getNotes(query, userId);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
            ((NotesRecyclerViewAdapter) list.getAdapter()).setNotes(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Note>> loader) {

        }
    }
}
