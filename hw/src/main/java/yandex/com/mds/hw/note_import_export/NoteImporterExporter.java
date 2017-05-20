package yandex.com.mds.hw.note_import_export;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yandex.com.mds.hw.db.NoteDao;
import yandex.com.mds.hw.db.NoteDaoImpl;
import yandex.com.mds.hw.models.Note;

import static yandex.com.mds.hw.utils.SerializationUtils.GSON;

public class NoteImporterExporter {
    private static final String TAG = NoteImporterExporter.class.getName();
    private static final int IMPORT_FLAG = 1;
    private static final int EXPORT_FLAG = 2;
    private static final int PROGRESS_FLAG = 3;

    public static final int SUCCESS_FLAG = 1;
    public static final int FAIL_FLAG = 2;

    private NoteDao noteDao;
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
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case IMPORT_FLAG: {
                        importListener.OnColorsImport((Integer) msg.obj);
                        break;
                    }
                    case EXPORT_FLAG: {
                        exportListener.OnColorsExport((Integer) msg.obj);
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
            try {
                Log.d(TAG, "Reading data from " + importFile);
                FileReader reader = new FileReader(importFile);
                Note[] records = GSON.fromJson(reader, Note[].class);
                noteDao.deleteNotes();
                noteDao.addNotes(records);
                mHandler.obtainMessage(IMPORT_FLAG, SUCCESS_FLAG).sendToTarget();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mHandler.obtainMessage(IMPORT_FLAG, FAIL_FLAG).sendToTarget();
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
                Note[] records = noteDao.getNotes();
                File exportFile = new File(context.getExternalFilesDir(null), filename);
                Log.d(TAG, "Writing data to " + exportFile);
                if (!exportFile.exists()) {
                    exportFile.createNewFile();
                }
                FileWriter writer = new FileWriter(exportFile);
                GSON.toJson(records, writer);
                writer.flush();
                writer.close();
                Message importCompletedMessage = mHandler.obtainMessage(EXPORT_FLAG, SUCCESS_FLAG);
                importCompletedMessage.sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                Message importCompletedMessage = mHandler.obtainMessage(EXPORT_FLAG, FAIL_FLAG);
                importCompletedMessage.sendToTarget();
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
        void OnColorsExport(int result);
    }

    interface OnColorsImportListener {
        void OnColorsImport(int result);
    }
}
