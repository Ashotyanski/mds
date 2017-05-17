package yandex.com.mds.hw.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.Date;

import yandex.com.mds.hw.MainApplication;
import yandex.com.mds.hw.colors.query.Query;
import yandex.com.mds.hw.colors.query.Utils;
import yandex.com.mds.hw.colors.query.clauses.DateFilter;
import yandex.com.mds.hw.colors.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.colors.query.clauses.Sort;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.utils.ArrayUtils;
import yandex.com.mds.hw.utils.TimeUtils;

import static android.provider.BaseColumns._ID;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.IMAGE_URL;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.OWNER_ID;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.SERVER_ID;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TITLE;

public class ColorDaoImpl implements ColorDao {
    private ColorDatabaseHelper dbHelper = ColorDatabaseHelper.getInstance(MainApplication.getContext());

    @Override
    public ColorRecord[] getColors() {
        return getColors(null, -1);
    }

    @Override
    public ColorRecord[] getColors(Query query, int userId) {
        Cursor c = getColorsCursor(query, -1);
        ColorRecord[] records = toRecords(c);
        c.close();
        return records;
    }

    @Override
    public Cursor getColorsCursor() {
        return getColorsCursor(null, -1);
    }

    @Override
    public Cursor getColorsCursor(Query query, int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = ColorDatabaseHelper.ALL_COLUMNS;
        String sortOrder = null;
        String selection = null;
        String[] selectionArgs = null;
        if (query != null) {
            Sort sort = query.getSort();
            if (sort != null)
                sortOrder = String.format("%s %s",
                        Utils.getDbColumnFromField(sort.getField()),
                        sort.isDescending() ? "DESC" : "ASC");

            DateFilter dateFilter = query.getDateFilter();
            DateIntervalFilter dateIntervalFilter = query.getDateIntervalFilter();
            if (dateFilter != null && dateIntervalFilter == null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateFilter.getDate());
                calendar.add(Calendar.HOUR, 1);
                selection = String.format("(%s BETWEEN ? AND ?)",
                        Utils.getDbColumnFromField(dateFilter.getField()));
                selectionArgs = new String[]{
                        String.valueOf(TimeUtils.trimToHours(dateFilter.getDate()).getTime()),
                        String.valueOf(TimeUtils.trimToHours(calendar.getTime()).getTime())};
            } else if (dateIntervalFilter != null && dateFilter == null) {
                selection = String.format("(%s BETWEEN ? AND ?)",
                        Utils.getDbColumnFromField(dateIntervalFilter.getField()));
                selectionArgs = new String[]{
                        String.valueOf(dateIntervalFilter.getFrom().getTime()),
                        String.valueOf(dateIntervalFilter.getTo().getTime())};
            }
            if (query.getSearch() != null) {
                String newSelection = String.format("(%s LIKE ? OR %s LIKE ?)", TITLE, DESCRIPTION);
                selection = selection == null ? newSelection : selection + " AND " + newSelection;

                String regex = "%" + query.getSearch() + "%";
                String[] newArgs = {regex, regex};
                selectionArgs = selectionArgs == null ? newArgs : ArrayUtils.concatStringArrays(selectionArgs, newArgs);
            }
            if (query.getColorFilter() != null) {
                String newSelection = String.format("(%s = ?)", COLOR);
                selection = selection == null ? newSelection : selection + " AND " + newSelection;
                String[] newArgs = {String.valueOf(query.getColorFilter().getColor())};
                selectionArgs = selectionArgs == null ? newArgs : ArrayUtils.concatStringArrays(selectionArgs, newArgs);
            }
        }
        if (userId > -1) {
            String newSelection = String.format("(%s = ?)", OWNER_ID);
            selection = selection == null ? newSelection : selection + " AND " + newSelection;
            String[] newArgs = {String.valueOf(userId)};
            selectionArgs = selectionArgs == null ? newArgs : ArrayUtils.concatStringArrays(selectionArgs, newArgs);
        }
        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public ColorRecord getColor(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = ColorDatabaseHelper.ALL_COLUMNS;
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        ColorRecord result = toRecord(c);
        c.close();
        return result;
    }

    @Override
    public long addColor(ColorRecord colorRecord) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = toContentValues(colorRecord, false);
        return db.insert(TABLE_NAME, null, contentValues);
    }

    @Override
    public boolean addColors(ColorRecord[] records) {
        boolean isAddedWithoutErrors = true;
        for (ColorRecord record : records) {
            if (addColor(record) > -1)
                isAddedWithoutErrors = false;
        }
        return isAddedWithoutErrors;
    }

    @Override
    public boolean saveColor(ColorRecord colorRecord) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = toContentValues(colorRecord, true);
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(colorRecord.getId())};

        return db.update(TABLE_NAME, contentValues, selection, selectionArgs) > 0;
    }

    @Override
    public boolean deleteColor(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, whereClause, selectionArgs);
        return false;
    }

    @Override
    public boolean deleteColors() {
        Cursor c = dbHelper.getWritableDatabase().rawQuery("DELETE from " + TABLE_NAME, null);
        while (c.moveToNext()) {
        }
        c.close();
        return false;
    }

    private ContentValues toContentValues(ColorRecord colorRecord, boolean isUpdate) {
        ContentValues contentValues = new ContentValues();
        if (isUpdate)
            contentValues.put(_ID, colorRecord.getId());
        contentValues.put(COLOR, colorRecord.getColor());
        contentValues.put(TITLE, colorRecord.getTitle());
        contentValues.put(DESCRIPTION, colorRecord.getDescription());
        if (colorRecord.getCreationDate() != null)
            contentValues.put(CREATION_DATE, colorRecord.getCreationDate().getTime());
        if (colorRecord.getLastModificationDate() != null)
            contentValues.put(LAST_MODIFICATION_DATE, colorRecord.getLastModificationDate().getTime());
        if (colorRecord.getLastViewDate() != null)
            contentValues.put(LAST_VIEW_DATE, colorRecord.getLastViewDate().getTime());
        contentValues.put(IMAGE_URL, colorRecord.getImageUrl());
        if (!isUpdate)
            contentValues.put(OWNER_ID, colorRecord.getOwnerId());
        contentValues.put(SERVER_ID, colorRecord.getServerId());
        return contentValues;
    }

    private ColorRecord toRecord(Cursor cursor) {
        ColorRecord colorRecord = new ColorRecord();
        colorRecord.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        colorRecord.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
        colorRecord.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        colorRecord.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        colorRecord.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(CREATION_DATE))));
        colorRecord.setLastModificationDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_MODIFICATION_DATE))));
        colorRecord.setLastViewDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_VIEW_DATE))));
        colorRecord.setImageUrl(cursor.getString(cursor.getColumnIndex(IMAGE_URL)));
        colorRecord.setOwnerId(cursor.getInt(cursor.getColumnIndex(OWNER_ID)));
        colorRecord.setServerId(cursor.getInt(cursor.getColumnIndex(SERVER_ID)));
        return colorRecord;
    }

    private ColorRecord[] toRecords(Cursor cursor) {
        ColorRecord[] colorRecord = new ColorRecord[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            colorRecord[i] = toRecord(cursor);
            i++;
        }
        return colorRecord;
    }
}