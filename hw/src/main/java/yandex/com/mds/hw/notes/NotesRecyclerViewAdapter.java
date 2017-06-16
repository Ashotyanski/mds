package yandex.com.mds.hw.notes;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.colorview.ColorView;
import yandex.com.mds.hw.models.Note;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder> {
    private List<Note> notes;

    private OnNoteSelectedListener noteSelectedListener;

    public NotesRecyclerViewAdapter(List<Note> notes) {
        this.notes = notes;
    }

    public NotesRecyclerViewAdapter(List<Note> notes, OnNoteSelectedListener noteSelectedListener) {
        this.notes = notes;
        this.noteSelectedListener = noteSelectedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_note, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String title = notes.get(position).getTitle();
        String description = notes.get(position).getDescription();
        int color = notes.get(position).getColor();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteSelectedListener.onNoteSelected(holder, notes.get(holder.getAdapterPosition()));
            }
        });
        holder.titleView.setText(title);
        holder.descriptionView.setText(description);
        holder.colorView.setColor(color);

        ViewCompat.setTransitionName(holder.colorView, String.valueOf(notes.get(position).getId()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleView;
        TextView descriptionView;
        ColorView colorView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.title);
            descriptionView = (TextView) itemView.findViewById(R.id.description);
            colorView = (ColorView) itemView.findViewById(R.id.filter_color);
        }
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void setOnClickListener(OnNoteSelectedListener noteSelectedListener) {
        this.noteSelectedListener = noteSelectedListener;
    }

    public interface OnNoteSelectedListener {
        void onNoteSelected(ViewHolder holder, Note note);
    }
}
