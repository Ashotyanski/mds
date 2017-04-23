package yandex.com.mds.hw3dev;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import yandex.com.mds.hw3dev.colorpicker.colorview.ColorView;

import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.TITLE;

public class ColorListAdapter extends CursorAdapter {
    public ColorListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));
        int color = cursor.getInt(cursor.getColumnIndexOrThrow(COLOR));

        holder.titleView.setText(title);
        holder.descriptionView.setText(description);
        holder.colorView.setColor(color);
    }


    public class ViewHolder {
        TextView titleView;
        TextView descriptionView;
        ColorView colorView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.title);
            descriptionView = (TextView) view.findViewById(R.id.description);
            colorView = (ColorView) view.findViewById(R.id.color);
        }
    }
}
