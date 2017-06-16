package yandex.com.mds.hw.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.DrawableRes;

import yandex.com.mds.hw.MainApplication;

public class NotificationUtils {
    public static void send(Notification notification, int id) {
        NotificationManager mNotificationManager = (NotificationManager) MainApplication.getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification);
    }

    public static Notification.Builder initNotificationBuilder(@DrawableRes int icon, String title, String text) {
        Notification.Builder builder = new Notification.Builder(MainApplication.getContext())
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text);
        return builder;
    }
}
