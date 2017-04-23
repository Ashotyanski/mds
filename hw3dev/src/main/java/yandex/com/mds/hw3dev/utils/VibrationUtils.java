package yandex.com.mds.hw3dev.utils;

import android.os.Vibrator;
import android.util.Log;

import static android.content.Context.VIBRATOR_SERVICE;
import static yandex.com.mds.hw3dev.MainApplication.getContext;

public class VibrationUtils {
    private static final int TIMEOUT_VIBRATE = 1000;
    public static final int VIBRATE_LONG = 50;
    public static final int VIBRATE_SHORT = 10;

    private static long lastVibrate = 0;

    public static void vibrate(int duration) {
        if (System.currentTimeMillis() - lastVibrate > TIMEOUT_VIBRATE) {
            Log.i("VIBRATION", "Vibrated for " + duration);
            ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(duration);
            lastVibrate = System.currentTimeMillis();
        }
    }
}
