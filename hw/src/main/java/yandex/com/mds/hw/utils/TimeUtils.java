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

    public static String toDateTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));
        calendar.setTimeZone(TimeZone.getDefault());
        Date date = calendar.getTime();
        return dateFormat.format(date);
    }
}
