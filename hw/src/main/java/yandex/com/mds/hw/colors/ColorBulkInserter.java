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
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private int totalCount = 100000;
    private int transactionSize = 200;

    private Context context;
    private ProgressDialog progress;
    private ColorDatabaseHelper databaseHelper;
    private ExecutorService service;
    private Handler mHandler;
    private OnInsertFinishListener listener;

    private static ColorBulkInserter instance;

    public static ColorBulkInserter getInstance(Context context) {
        if (instance == null)
            instance = new ColorBulkInserter(context);
        return instance;
    }

    private ColorBulkInserter(Context context) {
        this.context = context;
        databaseHelper = ColorDatabaseHelper.getInstance(context);
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        service = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, queue);
        mHandler = new Handler(Looper.myLooper()) {
            int i = totalCount / transactionSize;
            final Object lock = new Object();

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 2) {
                    progress.incrementProgressBy(transactionSize);
                    synchronized (lock) {
                        i--;
                    }
                    if (i == 0) {
                        Log.d(TAG, "Bulk inserted " + totalCount + " colors");
                        progress.dismiss();
                        if (listener != null)
                            listener.onFinishInsert();
                        i = totalCount / transactionSize;
                    }
                }
            }
        };
    }

    public void start() {
        progress = new ProgressDialog(context);
        progress.setMessage("Inserting colors");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(totalCount);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        for (int i = 0; i <= totalCount; i += transactionSize)
            service.execute(new BulkInsertRunnable(i, i + transactionSize));
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
                ContentValues contentValues;
                for (int i = from; i <= to; i++) {
                    contentValues = new ContentValues();
                    generateColorRecord(contentValues, i);
                    db.insert(TABLE_NAME, null, contentValues);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
            Log.d(TAG, "Transaction completed for " + from + " to " + to);
            Message completeMessage = mHandler.obtainMessage(2);
            completeMessage.sendToTarget();
        }

    }

    private void generateColorRecord(ContentValues contentValues, int seed) {
        contentValues.put(COLOR, -seed);
        contentValues.put(TITLE, String.valueOf(seed));
        contentValues.put(DESCRIPTION, "This is for " + String.valueOf(seed));
        contentValues.put(CREATION_DATE, new Date().getTime());
        contentValues.put(IMAGE_URL, "https://i.ytimg.com/vi/EJqY4bUM6PY/maxresdefault.jpg");
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setTransactionSize(int transactionSize) {
        this.transactionSize = transactionSize;
    }

    public void setOnInsertFinishListener(OnInsertFinishListener listener) {
        this.listener = listener;
    }

    public interface OnInsertFinishListener {
        void onFinishInsert();
    }
}