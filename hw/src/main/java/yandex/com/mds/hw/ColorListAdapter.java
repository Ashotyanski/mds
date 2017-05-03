package yandex.com.mds.hw;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import yandex.com.mds.hw.colorpicker.colorview.ColorView;
import yandex.com.mds.hw.db.ColorDatabaseHelper;

public class ColorListAdapter extends CursorAdapter {
    public ColorListAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        int id = cursor.getInt(cursor.getColumnIndex(ColorDatabaseHelper.ColorEntry._ID));
        holder.setId(id);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndexOrThrow(ColorDatabaseHelper.ColorEntry.TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(ColorDatabaseHelper.ColorEntry.DESCRIPTION));
        int color = cursor.getInt(cursor.getColumnIndexOrThrow(ColorDatabaseHelper.ColorEntry.COLOR));

        holder.titleView.setText(title);
        holder.descriptionView.setText(description);
        holder.colorView.setColor(color);
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
            colorView = (ColorView) view.findViewById(R.id.color);
        }
    }
}
