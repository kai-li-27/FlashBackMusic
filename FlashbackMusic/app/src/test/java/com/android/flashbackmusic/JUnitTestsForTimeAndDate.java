package com.android.flashbackmusic;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by kwmag on 3/14/2018.
 */

public class JUnitTestsForTimeAndDate {

    TimeAndDate timeAndDate;

    @Before
    public void initializeTimeAndDate() {
        timeAndDate = TimeAndDate.getTimeAndDate();
    }

    @Test
    public void setTimeToCustomTest() {
        int year = 2018;
        int month = 2;
        int dayOfMonth = 2;
        int hour = 8;
        int minute = 40;    // 2018-3-2 8:40

        timeAndDate.setTimeToCustom(year, month, dayOfMonth, hour, minute);
        // note, this uses logging, so used mock logging in src/test/java/android.util
        long timeMillis = timeAndDate.getDateSelected();
        assertEquals(1520008800000L, timeMillis);
        assertEquals(false, timeAndDate.isTimeCurrentTime());
    }

    @Test
    public void toStringTest() {
        int year = 2018;
        int month = 2;
        int dayOfMonth = 2;
        int hour = 8;
        int minute = 40;

        timeAndDate.setTimeToCustom(year, month, dayOfMonth, hour, minute);
        String dateSet = timeAndDate.toString();
        assertEquals("2018-03-02 08:40", dateSet);
    }
}
