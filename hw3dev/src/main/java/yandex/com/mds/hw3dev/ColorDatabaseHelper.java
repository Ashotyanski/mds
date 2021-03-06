package yandex.com.mds.hw3dev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.BaseColumns;

import static android.provider.BaseColumns._ID;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw3dev.ColorDatabaseHelper.ColorEntry.TITLE;


public class ColorDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "colors.db";

    private static final String CREATE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLOR + " INTEGER, " +
            TITLE + " VARCHAR(50), " +
            DESCRIPTION + " VARCHAR(50);";
    private static final String DELETE_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public ColorDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        for (int i = 0; i < 4; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(_ID, i);
            contentValues.put(COLOR, Color.LTGRAY);
            db.insert(TABLE_NAME, null, contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_QUERY);
        onCreate(db);
    }

    public Cursor getColors() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = new String[]{_ID, COLOR, TITLE, DESCRIPTION};
        String sortOrder = _ID + " ASC";

        return db.query(TABLE_NAME, columns, null, null, null, null, sortOrder);
    }

    public void saveColor(int id, int color, String title, String description) {
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

    public void addColor(int id, int color, String title, String description) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, id);
        contentValues.put(COLOR, color);
        contentValues.put(TITLE, title);
        contentValues.put(DESCRIPTION, description);

        db.insert(TABLE_NAME, null, contentValues);
    }

    public final class ColorEntry implements BaseColumns {
        public static final String TABLE_NAME = "colors";
        public static final String COLOR = "color";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
    }
}
