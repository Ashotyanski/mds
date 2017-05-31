package yandex.com.mds.hw.note_import_export;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.db.NoteDatabaseHelper;
import yandex.com.mds.hw.models.Note;

import static yandex.com.mds.hw.db.NoteDatabaseHelper.NoteEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.NoteDatabaseHelper.toContentValues;
import static yandex.com.mds.hw.utils.SerializationUtils.GSON;

public class NoteImporterExporter {
    private static final String TAG = NoteImporterExporter.class.getName();
    private static final int IMPORT_FLAG = 1;
    private static final int EXPORT_FLAG = 2;

    public static final String IMPORT_ACTION = "IMPORT";
    public static final IntentFilter importIntentFilter = new IntentFilter(IMPORT_ACTION);

    private NoteDao noteDao;
    private NoteDatabaseHelper dbHelper;
    private Context context;
    private Handler mHandler;
    private ExecutorService service;

    private OnColorsExportListener exportListener;
    private OnColorsImportListener importListener;

    private static NoteImporterExporter instance;

    public static NoteImporterExporter getInstance(Context c) {
        if (instance == null) {
            instance = new NoteImporterExporter(c);
        }
        return instance;
    }

    private NoteImporterExporter(final Context context) {
        this.context = context;
        noteDao = new NoteDaoImpl();
        dbHelper = NoteDatabaseHelper.getInstance(context);
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case IMPORT_FLAG: {
                        importListener.OnColorsImport((ImportExportStatus) msg.obj);
                        break;
                    }
                    case EXPORT_FLAG: {
                        exportListener.OnColorsExport((ImportExportStatus) msg.obj);
                        break;
                    }
                }
            }
        };
        service = Executors.newSingleThreadExecutor();
    }

    public void exportColors(String filename) {
        service.execute(new ColorExportTask(filename));
    }

    public void importColors(String filename) {
        service.execute(new ColorImportTask(filename));
    }

    private class ColorImportTask implements Runnable {
        String filename;

        ColorImportTask(String filename) {
            this.filename = filename;
        }

        @Override
        public void run() {
            File importFile = new File(context.getExternalFilesDir(null), filename);
            try {
                Log.d(TAG, "Reading data from " + importFile);
                mHandler.obtainMessage(IMPORT_FLAG, new ImportExportStatus(.0, "Reading notes...")).sendToTarget();
                FileReader reader = new FileReader(importFile);
                final List<Note> records = GSON.fromJson(reader, new TypeToken<List<Note>>() {
                }.getType());

                noteDao.deleteNotes();
                ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                int poolSize = 500;
                for (int i = 0; i < records.size(); i += poolSize) {
                    final int from = i, to = Math.min(i + poolSize, records.size());
                    Log.d(TAG, "Importing from " + from + " to " + to);
                    service.execute(new Runnable() {
                        @Override
                        public void run() {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            try {
                                db.beginTransaction();
                                for (int j = from; j < to; j++) {
                                    ContentValues contentValues = toContentValues(records.get(j), false);
                                    db.insert(TABLE_NAME, null, contentValues);
                                }
                                db.setTransactionSuccessful();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "run: Exception when importing");
                            } finally {
                                db.endTransaction();
                            }
                            if (to != records.size()) {
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus((double) to / records.size(), "Importing notes...")).sendToTarget();
                            } else {
                                Log.d(TAG, "Notes imported from " + filename);
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus(1, "Notes imported")).sendToTarget();
                                Intent intent = new Intent(IMPORT_ACTION);
                                context.sendBroadcast(intent);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not import notes from " + filename);
                mHandler.obtainMessage(IMPORT_FLAG, new ImportExportStatus(-1, "Import failed")).sendToTarget();
            }
        }
    }

    private class ColorExportTask implements Runnable {
        String filename;

        ColorExportTask(String filename) {
            this.filename = filename;
        }

        @Override
        public void run() {
            try {
                List<Note> records = noteDao.getNotes();
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(.0, "Fetching notes...")).sendToTarget();

                File exportFile = new File(context.getExternalFilesDir(null), filename);
                Log.d(TAG, "Writing data to " + exportFile);
                if (!exportFile.exists()) {
                    exportFile.createNewFile();
                }
                FileWriter writer = new FileWriter(exportFile);
                writer.write("[");
                for (int i = 0; i < records.size() - 1; i++) {
                    writer.write(GSON.toJson(records.get(i)));
                    writer.write(",");
                    if (i % 500 == 0) {
                        mHandler.obtainMessage(EXPORT_FLAG,
                                new ImportExportStatus((double) i / records.size(), "Writing colors...")).sendToTarget();
                        Log.d(TAG, "Writing data at " + i);
                        writer.flush();
                    }
                }
                writer.write(GSON.toJson(records.get(records.size() - 1)));
                writer.write("]");
                writer.flush();
                writer.close();

                Log.d(TAG, "Notes exported to " + exportFile);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(1.0, "Colors exported")).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not export notes to " + filename);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(-1.0, "Export failed")).sendToTarget();
            }
        }
    }

    public void setExportListener(OnColorsExportListener exportListener) {
        this.exportListener = exportListener;
    }

    public void setImportListener(OnColorsImportListener importListener) {
        this.importListener = importListener;
    }

    interface OnColorsExportListener {
        void OnColorsExport(ImportExportStatus progress);
    }

    interface OnColorsImportListener {
        void OnColorsImport(ImportExportStatus progress);
    }

    class ImportExportStatus {
        double progress;
        String message;

        public ImportExportStatus(double progress, String message) {
            this.progress = progress;
            this.message = message;
        }
    }
}
