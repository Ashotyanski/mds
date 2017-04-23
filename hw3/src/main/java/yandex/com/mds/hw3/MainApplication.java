package yandex.com.mds.hw3;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private static Context context = null;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
