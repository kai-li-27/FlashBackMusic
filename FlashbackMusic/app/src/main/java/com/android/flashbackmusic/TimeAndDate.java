package com.android.flashbackmusic;

import android.util.Log;
import android.widget.CalendarView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by kwmag on 3/11/2018.
 */

public class TimeAndDate {

    private long dateSelected;  // number of milliseconds since 1/1/1970 00:00.00
    private boolean isTimeCurrentTime;
    private static TimeAndDate instance;


    private TimeAndDate() {
        setTimeToCurrent();
    }

    /*
    // called when we want the time to be different from current time
    public TimeAndDate getTimeAndDate(CalendarView calendar, TimePicker timePicker){
        if (instance == null) {
            instance = new TimeAndDate(calendar, timePicker);
        }
        return instance;
    }
    */

    // called when we want the time to be current time
    public static TimeAndDate getTimeAndDate() {
        if (instance == null) {
            instance = new TimeAndDate();
        }
        return instance;
    }

    public void setTimeToCurrent(){
        dateSelected = System.currentTimeMillis();      // includes date and time in millis
        isTimeCurrentTime = true;
        Date date = new Date(dateSelected);
        Log.i("TimeAndDate", date.toString());
    }


    public void setTimeToCustom(int year, int month, int dayOfMonth, int hour, int minute) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        try {
            date = format.parse("" + year + "-" + (month + 1) + "-" + dayOfMonth + " " + hour + ":" + minute);
            Log.i("TimeAndDate", "" + year + "-" + (month + 1) + "-" + dayOfMonth + " " + hour + ":" + minute);
            Log.i("TimeAndDate", date.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.dateSelected = date.getTime();
        isTimeCurrentTime = false;
    }

    public long getDateSelected() {
        return dateSelected;
    }

    public void setDateSelected(long dateSelected) {
        this.dateSelected = dateSelected;
    }


    public boolean isTimeCurrentTime() {
        return isTimeCurrentTime;
    }

    public void setTimeCurrentTime(boolean timeCurrentTime) {
        isTimeCurrentTime = timeCurrentTime;
    }

    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(dateSelected);
        if (isTimeCurrentTime() == false) {
            return format.format(date);
        }
        return "Current";
    }
}
