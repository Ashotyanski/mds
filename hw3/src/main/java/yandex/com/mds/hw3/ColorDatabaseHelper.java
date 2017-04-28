package yandex.com.mds.hw3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.BaseColumns;

import java.util.HashMap;

import static android.provider.BaseColumns._ID;
import static yandex.com.mds.hw3.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw3.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw3.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw3.ColorDatabaseHelper.ColorEntry.TITLE;


public class ColorDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String CREATE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            TITLE + " VARCHAR(50), " +
            DESCRIPTION + " VARCHAR(50), " +
            COLOR + " INTEGER);";

    private static final String DATABASE_NAME = "colors.db";
    private static final String DELETE_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public ColorDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        HashMap<String, Integer> colors = new HashMap<>();
        colors.put("Red", Color.RED);
        colors.put("Yellow", Color.YELLOW);
        colors.put("Blue", Color.BLUE);
        colors.put("Green", Color.GREEN);
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

    public Cursor getColors() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {_ID, TITLE, DESCRIPTION, COLOR};
        String sortOrder = _ID + " ASC";
        return db.query(TABLE_NAME, columns, null, null, null, null, sortOrder);
    }

    public Cursor getColor(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {_ID, TITLE, DESCRIPTION, COLOR};
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
    }

    public void saveColor(int id, String title, String description, int color) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, id);
        contentValues.put(COLOR, color);
        contentValues.put(TITLE, title);
        contentValues.put(DESCRIPTION, description);

        String selection = _ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(TABLE_NAME, contentValues, selection, selectionArgs);
    }

    public void addColor(String title, String description, int color) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(DESCRIPTION, description);
        contentValues.put(COLOR, color);

        db.insert(TABLE_NAME, null, contentValues);
    }

    public void deleteColor(int colorId) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(colorId)};
        db.delete(TABLE_NAME, whereClause, selectionArgs);
    }

    public final class ColorEntry implements BaseColumns {
        public static final String TABLE_NAME = "colors";
        public static final String COLOR = "color";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
    }
}
