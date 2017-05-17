package yandex.com.mds.hw.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.Date;

import yandex.com.mds.hw.MainApplication;
import yandex.com.mds.hw.notes.query.Query;
import yandex.com.mds.hw.notes.query.Utils;
import yandex.com.mds.hw.notes.query.clauses.DateFilter;
import yandex.com.mds.hw.notes.query.clauses.DateIntervalFilter;
import yandex.com.mds.hw.notes.query.clauses.Sort;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.utils.ArrayUtils;
import yandex.com.mds.hw.utils.TimeUtils;

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

public class NoteDaoImpl implements NoteDao {
    private NoteDatabaseHelper dbHelper = NoteDatabaseHelper.getInstance(MainApplication.getContext());

    @Override
    public Note[] getNotes() {
        return getNotes(null, -1);
    }

    @Override
    public Note[] getNotes(Query query, int userId) {
        Cursor c = getNotesCursor(query, userId);
        Note[] records = toRecords(c);
        c.close();
        return records;
    }

    @Override
    public Cursor getNotesCursor() {
        return getNotesCursor(null, -1);
    }

    @Override
    public Cursor getNotesCursor(Query query, int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = NoteDatabaseHelper.ALL_COLUMNS;
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
    public Note getNote(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = NoteDatabaseHelper.ALL_COLUMNS;
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor c = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        Note result = NoteDatabaseHelper.toRecord(c);
        c.close();
        return result;
    }

    @Override
    public long addNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = NoteDatabaseHelper.toContentValues(note, false);
        return db.insert(TABLE_NAME, null, contentValues);
    }

    @Override
    public boolean addNotes(Note[] records) {
        boolean isAddedWithoutErrors = true;
        for (Note record : records) {
            if (addNote(record) > -1)
                isAddedWithoutErrors = false;
        }
        return isAddedWithoutErrors;
    }

    @Override
    public boolean saveNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = NoteDatabaseHelper.toContentValues(note, true);
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.getId())};

        return db.update(TABLE_NAME, contentValues, selection, selectionArgs) > 0;
    }

    @Override
    public boolean deleteNote(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(TABLE_NAME, whereClause, selectionArgs);
        return false;
    }

    @Override
    public boolean deleteNotes() {
        Cursor c = dbHelper.getWritableDatabase().rawQuery("DELETE from " + TABLE_NAME, null);
        while (c.moveToNext()) {
        }
        c.close();
        return false;
    }

    private ContentValues toContentValues(Note note, boolean isUpdate) {
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
        if (!isUpdate)
            contentValues.put(OWNER_ID, note.getOwnerId());
        contentValues.put(SERVER_ID, note.getServerId());
        return contentValues;
    }

    private Note toRecord(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        note.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
        note.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        note.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
        note.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(CREATION_DATE))));
        note.setLastModificationDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_MODIFICATION_DATE))));
        note.setLastViewDate(new Date(cursor.getLong(cursor.getColumnIndex(LAST_VIEW_DATE))));
        note.setImageUrl(cursor.getString(cursor.getColumnIndex(IMAGE_URL)));
        note.setOwnerId(cursor.getInt(cursor.getColumnIndex(OWNER_ID)));
        note.setServerId(cursor.getInt(cursor.getColumnIndex(SERVER_ID)));
        return note;
    }

    private Note[] toRecords(Cursor cursor) {
        Note[] note = new Note[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            note[i] = NoteDatabaseHelper.toRecord(cursor);
            i++;
        }
        return note;
    }
}