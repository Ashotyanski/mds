package yandex.com.mds.hw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Date;
import java.util.HashMap;

import yandex.com.mds.hw.models.ColorRecord;

import static android.provider.BaseColumns._ID;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.CREATION_DATE;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw.ColorDatabaseHelper.ColorEntry.TITLE;


public class ColorDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            TITLE + " VARCHAR(50), " +
            DESCRIPTION + " VARCHAR(50), " +
            COLOR + " INTEGER," +
            CREATION_DATE + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            LAST_MODIFICATION_DATE + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
            LAST_VIEW_DATE + " INTEGER" +
            ");";

    private static final String DATABASE_NAME = "colors.db";
    private static final String DELETE_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public ColorDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        HashMap<String, Integer> colors = new HashMap<>();
        colors.put("Red", 0xFFFF0000);
        colors.put("Yellow", 0xFFFFFF00);
        colors.put("Blue", 0xFF0000FF);
        colors.put("Green", 0xFF00FF00);
        int i = 0;
        for (String colorName : colors.keySet()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(_ID, i);
            contentValues.put(TITLE, colorName);
            contentValues.put(DESCRIPTION, "This is for " + colorName.toLowerCase() + " color");
            contentValues.put(COLOR, colors.get(colorName));
            db.insert(TABLE_NAME, null, contentValues);
            i++;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_QUERY);
        onCreate(db);
    }

    public Cursor getColors(boolean sortAlphabetically) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {_ID, TITLE, DESCRIPTION, COLOR, CREATION_DATE, LAST_MODIFICATION_DATE, LAST_VIEW_DATE};
        String sortOrder = _ID + " ASC";
        if (sortAlphabetically) {
            sortOrder = TITLE + " ASC";
        }
        return db.query(TABLE_NAME, columns, null, null, null, null, sortOrder);
    }

    public Cursor getColor(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {_ID, TITLE, DESCRIPTION, COLOR, CREATION_DATE, LAST_MODIFICATION_DATE, LAST_VIEW_DATE};
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
    }

    public void saveColor(ColorRecord colorRecord) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
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

        String selection = _ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(colorRecord.getId())};

        db.update(TABLE_NAME, contentValues, selection, selectionArgs);
    }

    public void addColor(ColorRecord colorRecord) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, colorRecord.getTitle());
        contentValues.put(DESCRIPTION, colorRecord.getDescription());
        contentValues.put(COLOR, colorRecord.getColor());
        if (colorRecord.getCreationDate() != null)
            contentValues.put(CREATION_DATE, colorRecord.getCreationDate().getTime());
        if (colorRecord.getLastModificationDate() != null)
            contentValues.put(LAST_MODIFICATION_DATE, colorRecord.getLastModificationDate().getTime());
        if (colorRecord.getLastViewDate() != null)
            contentValues.put(LAST_VIEW_DATE, colorRecord.getLastViewDate().getTime());

        db.insert(TABLE_NAME, null, contentValues);
    }

    public void deleteColor(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, whereClause, selectionArgs);
    }

    public static ColorRecord fromCursor(Cursor cursor) {
        ColorRecord colorRecord = new ColorRecord();
        cursor.moveToFirst();
        colorRecord.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        colorRecord.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
        colorRecord.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        colorRecord.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        colorRecord.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(CREATION_DATE))));
        colorRecord.setLastModificationDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_MODIFICATION_DATE))));
        colorRecord.setLastViewDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_VIEW_DATE))));
        return colorRecord;
    }

    public final class ColorEntry implements BaseColumns {
        public static final String TABLE_NAME = "colors";
        public static final String COLOR = "color";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String CREATION_DATE = "creationDate";
        public static final String LAST_MODIFICATION_DATE = "lastModificationDate";
        public static final String LAST_VIEW_DATE = "lastViewDate";
    }
}
