package yandex.com.mds.hw.utils;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;

public class TimeUtilsTest {
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    @Before
    public void setUp() throws Exception {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 100);
    }

    @Test
    public void testTrimMilliseconds() throws Exception {
        assertEquals("2000-01-01 01:01:01.000", dateFormat.format(TimeUtils.trimMilliseconds(calendar.getTime())));
    }

    @Test
    public void testTrimToHours() throws Exception {
        assertEquals("2000-01-01 01:00:00.000", dateFormat.format(TimeUtils.trimToHours(calendar.getTime())));
    }
}
