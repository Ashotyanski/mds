package yandex.com.mds.hw.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Date;
import java.util.HashMap;

import yandex.com.mds.hw.models.Note;

import static android.provider.BaseColumns._ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.COLOR;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.DESCRIPTION;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.IMAGE_URL;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_MODIFICATION_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.LAST_VIEW_DATE;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.OWNER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.SERVER_ID;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TITLE;


public class NoteDatabaseHelper extends SQLiteOpenHelper {
    public static final String[] ALL_COLUMNS = {_ID, TITLE, DESCRIPTION, COLOR, CREATION_DATE, LAST_MODIFICATION_DATE, LAST_VIEW_DATE, CREATION_DATE, IMAGE_URL, OWNER_ID, SERVER_ID};
    private static final int DATABASE_VERSION = 4;
    private static final String CREATE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            TITLE + " VARCHAR(50), " +
            DESCRIPTION + " VARCHAR(50), " +
            COLOR + " INTEGER," +
            CREATION_DATE + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            LAST_MODIFICATION_DATE + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
            LAST_VIEW_DATE + " INTEGER," +
            IMAGE_URL + " TEXT," +
            OWNER_ID + " INTEGER, " +
            SERVER_ID + " INTEGER DEFAULT -1" +
            ");";

    private static final String DATABASE_NAME = "notes.db";
    private static final String DELETE_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static NoteDatabaseHelper instance;

    public static NoteDatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new NoteDatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private NoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        HashMap<String, Integer> notes = new HashMap<>();
        notes.put("Red", 0xFFFF0000);
        notes.put("Yellow", 0xFFFFFF00);
        notes.put("Blue", 0xFF0000FF);
        notes.put("Green", 0xFF00FF00);
        int i = 0;
        for (String colorName : notes.keySet()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(_ID, i);
            contentValues.put(TITLE, colorName);
            contentValues.put(DESCRIPTION, "This is for " + colorName.toLowerCase() + " color");
            contentValues.put(COLOR, notes.get(colorName));
            contentValues.put(OWNER_ID, i);
            db.insert(TABLE_NAME, null, contentValues);
            i++;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_QUERY);
        onCreate(db);
    }

    public static ContentValues toContentValues(Note note, boolean isUpdate) {
        ContentValues contentValues = new ContentValues();
        if (isUpdate)
            contentValues.put(_ID, note.getId());
        contentValues.put(COLOR, note.getColor());
        contentValues.put(TITLE, note.getTitle());
        contentValues.put(DESCRIPTION, note.getDescription());
        if (note.getCreationDate() != null)
            contentValues.put(CREATION_DATE, note.getCreationDate().getTime());
        if (note.getLastModificationDate() != null)
            contentValues.put(LAST_MODIFICATION_DATE, note.getLastModificationDate().getTime());
        if (note.getLastViewDate() != null)
            contentValues.put(LAST_VIEW_DATE, note.getLastViewDate().getTime());
        contentValues.put(IMAGE_URL, note.getImageUrl());
        return contentValues;
    }

    public static Note toRecord(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        note.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
        note.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        note.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        note.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(CREATION_DATE))));
        note.setLastModificationDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_MODIFICATION_DATE))));
        note.setLastViewDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_VIEW_DATE))));
        note.setImageUrl(cursor.getString(cursor.getColumnIndex(IMAGE_URL)));
        return note;
    }

    public final class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLOR = "color";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String CREATION_DATE = "creationDate";
        public static final String LAST_MODIFICATION_DATE = "lastModificationDate";
        public static final String LAST_VIEW_DATE = "lastViewDate";
        public static final String IMAGE_URL = "imageUrl";
        public static final String OWNER_ID = "owner_id";
        public static final String SERVER_ID = "server_id";

        private NoteEntry() {
        }
    }
}
