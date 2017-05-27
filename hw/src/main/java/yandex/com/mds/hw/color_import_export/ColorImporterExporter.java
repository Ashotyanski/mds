package yandex.com.mds.hw.color_import_export;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.db.ColorDatabaseHelper;
import yandex.com.mds.hw.models.ColorRecord;

import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.toContentValues;
import static yandex.com.mds.hw.utils.SerializationUtils.GSON;

public class ColorImporterExporter {
    private static final String TAG = ColorImporterExporter.class.getName();
    private static final int IMPORT_FLAG = 1;
    private static final int EXPORT_FLAG = 2;

    public static final String IMPORT_ACTION = "IMPORT";
    public static final IntentFilter importIntentFilter = new IntentFilter(IMPORT_ACTION);

    private ColorDao colorDao;
    private ColorDatabaseHelper dbHelper;
    private Context context;
    private Handler mHandler;
    private ExecutorService service;

    private OnColorsExportListener exportListener;
    private OnColorsImportListener importListener;

    private static ColorImporterExporter instance;

    public static ColorImporterExporter getInstance(Context c) {
        if (instance == null) {
            instance = new ColorImporterExporter(c);
        }
        return instance;
    }

    private ColorImporterExporter(final Context context) {
        this.context = context;
        dbHelper = ColorDatabaseHelper.getInstance(context);
        colorDao = new ColorDaoImpl();
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
                mHandler.obtainMessage(IMPORT_FLAG, new ImportExportStatus(.0, "Reading colors...")).sendToTarget();
                FileReader reader = new FileReader(importFile);
                final ColorRecord[] records = GSON.fromJson(reader, ColorRecord[].class);

                colorDao.deleteColors();
                ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                int poolSize = 500;
                for (int i = 0; i < records.length; i += poolSize) {
                    final int from = i, to = Math.min(i + poolSize, records.length);
                    Log.d(TAG, "Importing from " + from + " to " + to);
                    service.execute(new Runnable() {
                        @Override
                        public void run() {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            try {
                                db.beginTransaction();
                                for (int j = from; j < to; j++) {
                                    ContentValues contentValues = toContentValues(records[j], false);
                                    db.insert(TABLE_NAME, null, contentValues);
                                }
                                db.setTransactionSuccessful();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "run: Exception when importing");
                            } finally {
                                db.endTransaction();
                            }
                            if (to != records.length) {
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus((double) to / records.length, "Importing colors...")).sendToTarget();
                            } else {
                                Log.d(TAG, "Colors imported from " + filename);
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus(1, "Colors imported")).sendToTarget();
                                Intent intent = new Intent(IMPORT_ACTION);
                                context.sendBroadcast(intent);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not import colors from " + filename);
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
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(.0, "Fetching colors...")).sendToTarget();
                ColorRecord[] records = colorDao.getColors();

                File exportFile = new File(context.getExternalFilesDir(null), filename);
                Log.d(TAG, "Writing data to " + exportFile);
                if (!exportFile.exists()) {
                    exportFile.createNewFile();
                }
                FileWriter writer = new FileWriter(exportFile);
                writer.write("[");
                for (int i = 0; i < records.length - 1; i++) {
                    writer.write(GSON.toJson(records[i]));
                    writer.write(",");
                    if (i % 500 == 0) {
                        mHandler.obtainMessage(EXPORT_FLAG,
                                new ImportExportStatus((double) i / records.length, "Writing colors...")).sendToTarget();
                        Log.d(TAG, "Writing data at " + i);
                        writer.flush();
                    }
                }
                writer.write(GSON.toJson(records[records.length - 1]));
                writer.write("]");
                writer.flush();
                writer.close();

                Log.d(TAG, "Colors exported to " + exportFile);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(1.0, "Colors exported")).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not export colors to " + filename);
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
