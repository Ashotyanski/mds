package yandex.com.mds.hw.db;

import android.database.Cursor;

import yandex.com.mds.hw.colors.query.Query;
import yandex.com.mds.hw.models.ColorRecord;

public interface ColorDao {
    ColorRecord[] getColors();

    ColorRecord[] getColors(Query query, int userId);

    Cursor getColorsCursor();

    Cursor getColorsCursor(Query query, int userId);

    ColorRecord getColor(int id);

    long addColor(ColorRecord record);

    boolean addColors(ColorRecord[] records);

    boolean saveColor(ColorRecord record);

    boolean deleteColor(int id);

    boolean deleteColors();
}
