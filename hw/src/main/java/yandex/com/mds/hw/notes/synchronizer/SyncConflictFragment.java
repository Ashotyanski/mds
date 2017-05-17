package yandex.com.mds.hw.notes.synchronizer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.colorview.ColorView;
import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;

public class SyncConflictFragment extends DialogFragment {
    private static final String TAG = SyncConflictFragment.class.getName();

    private ListView listView;
    private ArrayList<ConflictNotes> conflictNotes;
    private NoteDao noteDao;
    private NoteSynchronizer synchronizer;

    private OnAllConflictsResolvedListener conflictsResolvedListener;

    public SyncConflictFragment() {
    }

    public static SyncConflictFragment newInstance(ArrayList<ConflictNotes> conflctNotes) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(NoteSynchronizationService.SYNC_CONFLICT_NOTES, conflctNotes);
        SyncConflictFragment fragment = new SyncConflictFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conflictNotes = getArguments().getParcelableArrayList(NoteSynchronizationService.SYNC_CONFLICT_NOTES);
        Log.d(TAG, conflictNotes.toString());
        noteDao = new NoteDaoImpl();
        synchronizer = NoteSynchronizer.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = new ListView(getActivity());
        return listView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ConflictNotesAdapter adapter = new ConflictNotesAdapter(getActivity(), R.layout.list_item_conflict_note, conflictNotes);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final ConflictNotes conflictNotes = ((ConflictNotesAdapter.ViewHolder) view.getTag()).getConflictNotes();
                ConflictNotesDialog dialog = new ConflictNotesDialog(getActivity(),
                        conflictNotes);
                dialog.setOnConflictResolvedListener(new ConflictNotesDialog.OnConflictResolvedListener() {
                    @Override
                    public void onConflictResolved(int result) {
                        ((ConflictNotesAdapter) listView.getAdapter()).remove(conflictNotes);
                        if (result > 0) {
                            if (conflictNotes.getRemote() == null)
                                noteDao.deleteNote(conflictNotes.getLocal().getId());
                            else {
                                conflictNotes.getRemote().setServerId(conflictNotes.getRemote().getId());
                                if (conflictNotes.getLocal() == null)
                                    noteDao.addNote(conflictNotes.getRemote());
                                else {
                                    conflictNotes.getRemote().setId(conflictNotes.getLocal().getId());
                                    noteDao.saveNote(conflictNotes.getRemote());
                                }
                            }
                        } else if (result < 0) {
                            if (conflictNotes.getLocal() == null)
                                synchronizer.deleteAsync(conflictNotes.getRemote());
                            else if (conflictNotes.getRemote() == null)
                                synchronizer.addAsync(conflictNotes.getLocal());
                            else {
                                synchronizer.saveAsync(conflictNotes.getLocal());
                            }
                        }
                        if (listView.getAdapter().isEmpty()) {
                            conflictsResolvedListener.onAllConflictsResolved();
                            dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAllConflictsResolvedListener)
            conflictsResolvedListener = (OnAllConflictsResolvedListener) context;
        else
            throw new RuntimeException("Context must implement " + OnAllConflictsResolvedListener.class.getName());
    }

    private static class ConflictNotesAdapter extends ArrayAdapter<ConflictNotes> {
        public ConflictNotesAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ConflictNotes> objects) {
            super(context, resource, objects);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            ConflictNotes rowItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_conflict_note, parent, false);
                holder = new ViewHolder(convertView);
                holder.setConflictNotes(rowItem);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.setConflictNotes(rowItem);
            }

            holder.titleView.setText(rowItem.getLocal().getTitle());
            holder.lastModificationDateView.setText(rowItem.getLocal().getLastModificationDate().toString());
            holder.colorView.setColor(rowItem.getLocal().getColor());
            return convertView;
        }

        class ViewHolder {
            ConflictNotes conflictNotes;
            TextView titleView;
            TextView lastModificationDateView;
            ColorView colorView;

            void setConflictNotes(ConflictNotes conflictNotes) {
                this.conflictNotes = conflictNotes;
            }

            ConflictNotes getConflictNotes() {
                return conflictNotes;
            }

            ViewHolder(View view) {
                titleView = (TextView) view.findViewById(R.id.title);
                lastModificationDateView = (TextView) view.findViewById(R.id.last_modification_date);
                colorView = (ColorView) view.findViewById(R.id.color);
            }
        }
    }

    public interface OnAllConflictsResolvedListener {
        void onAllConflictsResolved();
    }
}
