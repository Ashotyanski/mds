package yandex.com.mds.hw.utils;

import android.os.Vibrator;
import android.util.Log;

import yandex.com.mds.hw.MainApplication;

import static android.content.Context.VIBRATOR_SERVICE;

public class VibrationUtils {
    private static final int TIMEOUT_VIBRATE = 1000;
    public static final int VIBRATE_LONG = 50;
    public static final int VIBRATE_SHORT = 10;

    private static long lastVibrateMillis = 0;

    public static void vibrate(int duration) {
        if (System.currentTimeMillis() - lastVibrateMillis > TIMEOUT_VIBRATE) {
            Log.i("VIBRATION", "Vibrated for " + duration);
            ((Vibrator) MainApplication.getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(duration);
            lastVibrateMillis = System.currentTimeMillis();
        }
    }
}
