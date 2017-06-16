package yandex.com.mds.hw.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import yandex.com.mds.hw.MainApplication;

public class NetworkUtils {
    public static boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) MainApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo connection = connMgr.getActiveNetworkInfo();
        return connection != null && connection.isConnected();
    }
}
