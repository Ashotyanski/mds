package yandex.com.mds.hw.color_import_export;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
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

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.db.ColorDao;
import yandex.com.mds.hw.db.ColorDaoImpl;
import yandex.com.mds.hw.db.ColorDatabaseHelper;
import yandex.com.mds.hw.models.ColorRecord;
import yandex.com.mds.hw.utils.NotificationUtils;

import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.toContentValues;
import static yandex.com.mds.hw.utils.SerializationUtils.GSON;

public class ColorImporterExporter {
    private static final String TAG = ColorImporterExporter.class.getName();
    private static final int IMPORT_FLAG = 1;
    private static final int EXPORT_FLAG = 2;
    private static final int PROGRESS_FLAG = 3;

    public static final int SUCCESS_FLAG = 1;
    public static final int FAIL_FLAG = 2;

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
                    case PROGRESS_FLAG: {
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
            Notification.Builder builder;
            try {
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors import", "Reading colors...");
                builder.setProgress(1, 0, false);
                NotificationUtils.send(builder.build(), 1);
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
                                Notification.Builder builder = NotificationUtils
                                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors import", "Importing colors...");
                                builder.setProgress(records.length, to, false);
                                NotificationUtils.send(builder.build(), 1);
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus((double) to / records.length, "Importing colors...")).sendToTarget();
                            } else {
                                Notification.Builder builder = NotificationUtils
                                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors import", "Colors imported");
                                builder.setProgress(0, 0, false);
                                NotificationUtils.send(builder.build(), 1);
                                Log.d(TAG, "Colors imported from " + filename);
                                mHandler.obtainMessage(IMPORT_FLAG,
                                        new ImportExportStatus(1, "Colors imported")).sendToTarget();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not import colors from " + filename);
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors import", "Import failed");
                builder.setProgress(0, 0, false);
                NotificationUtils.send(builder.build(), 1);
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
            Notification.Builder builder = null;
            try {
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(.0, "Colors export")).sendToTarget();
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors export", "Fetching colors");
                builder.setProgress(2, 0, false);
                NotificationUtils.send(builder.build(), 2);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(.0, "Fetching colors")).sendToTarget();
                ColorRecord[] records = colorDao.getColors();

                File exportFile = new File(context.getExternalFilesDir(null), filename);
                Log.d(TAG, "Writing data to " + exportFile);
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors export", "Writing colors...");
                builder.setProgress(2, 1, false);
                NotificationUtils.send(builder.build(), 2);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(0.5, "Writing colors...")).sendToTarget();
                if (!exportFile.exists()) {
                    exportFile.createNewFile();
                }
                FileWriter writer = new FileWriter(exportFile);
                GSON.toJson(records, writer);
                writer.flush();
                writer.close();

                Log.d(TAG, "Colors exported to " + exportFile);
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors export", "Colors exported");
                builder.setProgress(0, 0, false);
                NotificationUtils.send(builder.build(), 2);
                mHandler.obtainMessage(EXPORT_FLAG, new ImportExportStatus(1.0, "Colors exported")).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Could not export colors to " + filename);
                builder = NotificationUtils
                        .initNotificationBuilder(R.drawable.ic_import_export, "Colors export", "Export failed");
                builder.setProgress(0, 0, false);
                NotificationUtils.send(builder.build(), 2);
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
