package yandex.com.mds.hw.colors;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import yandex.com.mds.hw.db.ColorDatabaseHelper;

import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.COLOR;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.CREATION_DATE;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.DESCRIPTION;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.IMAGE_URL;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TABLE_NAME;
import static yandex.com.mds.hw.db.ColorDatabaseHelper.ColorEntry.TITLE;

class ColorBulkInserter {
    private static final String TAG = ColorBulkInserter.class.getName();
    private static final int TOTAL_COUNT = 100000;
    private static final int TRANSACTION_SIZE = 200;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private Context context;
    private ProgressDialog progress;
    private ColorDatabaseHelper databaseHelper;
    private ExecutorService service;
    private Handler mHandler;

    private OnInsertFinishListener listener;

    public ColorBulkInserter(Context context, OnInsertFinishListener listener) {
        this.context = context;
        this.listener = listener;
        databaseHelper = ColorDatabaseHelper.getInstance(context);
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        service = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, queue);

        mHandler = new Handler(Looper.myLooper()) {
            int i = TOTAL_COUNT / TRANSACTION_SIZE;
            final Object lock = new Object();

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 2) {
                    progress.incrementProgressBy(TRANSACTION_SIZE);
                    synchronized (lock) {
                        i--;
                    }
                    if (i == 0) {
                        Log.d(TAG, "Bulk inserted " + TOTAL_COUNT + " colors");
                        progress.dismiss();
                        ColorBulkInserter.this.listener.onFinishInsert();
                        i = TOTAL_COUNT / TRANSACTION_SIZE;
                    }
                }
            }
        };
    }

    public void start() {
        progress = new ProgressDialog(context);
        progress.setMessage("Inserting colors");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(TOTAL_COUNT);
        progress.show();

        for (int i = 0; i <= TOTAL_COUNT; i += TRANSACTION_SIZE)
            service.execute(new BulkInsertRunnable(i, i + TRANSACTION_SIZE));
    }

    private class BulkInsertRunnable implements Runnable {
        int from, to;

        public BulkInsertRunnable(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void run() {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (int i = from; i <= to; i++) {
                    ContentValues contentValues = generateColorRecord(i);
                    db.insert(TABLE_NAME, null, contentValues);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
//                db.close();
            }
            Log.d(TAG, "Transaction completed for " + from + " to " + to);
            Message completeMessage = mHandler.obtainMessage(2);
            completeMessage.sendToTarget();
        }
    }

    private ContentValues generateColorRecord(int seed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOR, seed);
        contentValues.put(TITLE, String.valueOf(seed));
        contentValues.put(DESCRIPTION, "This is for " + String.valueOf(seed));
        contentValues.put(CREATION_DATE, new Date().getTime());
        contentValues.put(IMAGE_URL, "https://yandex.ru/search/xml?&key=qwerty&query=" + String.valueOf(seed));
        return contentValues;
    }

    public interface OnInsertFinishListener {
        void onFinishInsert();
    }
}