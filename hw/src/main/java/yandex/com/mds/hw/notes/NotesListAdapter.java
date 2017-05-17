package yandex.com.mds.hw.notes;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.colorview.ColorView;
import yandex.com.mds.hw.db.NoteDatabaseHelper;

public class NotesListAdapter extends CursorAdapter {
    public NotesListAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_note, parent, false);
        ViewHolder holder = new ViewHolder(v);
        int id = cursor.getInt(cursor.getColumnIndex(NoteDatabaseHelper.NoteEntry._ID));
        holder.setId(id);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.NoteEntry.TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.NoteEntry.DESCRIPTION));
        int color = cursor.getInt(cursor.getColumnIndexOrThrow(NoteDatabaseHelper.NoteEntry.COLOR));
        int id = cursor.getInt(cursor.getColumnIndex(NoteDatabaseHelper.NoteEntry._ID));

        holder.titleView.setText(title);
        holder.descriptionView.setText(description);
        holder.colorView.setColor(color);
        holder.setId(id);
    }

    public class ViewHolder {
        int id;
        TextView titleView;
        TextView descriptionView;
        ColorView colorView;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.title);
            descriptionView = (TextView) view.findViewById(R.id.description);
            colorView = (ColorView) view.findViewById(R.id.filter_color);
        }
    }
}
