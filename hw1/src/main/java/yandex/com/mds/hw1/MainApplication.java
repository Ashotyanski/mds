package yandex.com.mds.hw1;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class MainApplication extends Application {

    private static final String TAG = "APPLICATION";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Terminated");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Config changed");
    }
}
