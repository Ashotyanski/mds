package yandex.com.mds.hw.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static final SimpleDateFormat IsoDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss ZZZZZ", Locale.getDefault());

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public static final SimpleDateFormat dateFormatWithoutSeconds = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm", Locale.getDefault());

    public static final SimpleDateFormat dateFormatOnlyHours = new SimpleDateFormat(
            "yyyy-MM-dd HH", Locale.getDefault());


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

    public static Date trimToHours(Date date) {
        try {
            return dateFormatOnlyHours.parse(dateFormatOnlyHours.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
