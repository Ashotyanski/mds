package yandex.com.mds.hw.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static long getTimestamp() {
        return new Date().getTime();
    }

    public static String formatDateTime(long timestamp) {
        return formatDateTime(new Date(timestamp));
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date, TimeZone.getTimeZone("UTC"));
    }

    public static String formatDateTime(Date date, TimeZone timeZone) {
        if (date == null)
            return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(timeZone);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(calendar.getTime());
    }
}
