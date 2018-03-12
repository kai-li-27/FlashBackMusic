package com.android.flashbackmusic;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SetAppTimeActivity extends AppCompatActivity {
    private int year = -1;
    private int month = -1;
    private int dayOfMonth = -1;
    private int hour = -1;
    private int minute = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_app_time);

        CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                year = i;
                month = i1;
                dayOfMonth = i2;
                Toast.makeText(App.getContext(),"clickity", Toast.LENGTH_SHORT).show();
            }
        });

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                hour = i;
                minute = i1;
            }
        });



        Button save = (Button) findViewById(R.id.save_day_time_button);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);

                if (hour == -1 || minute == -1) {
                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                }
                if (year == -1 || month == -1 || dayOfMonth == -1) {
                    Calendar cal = Calendar.getInstance();
                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    Toast.makeText(App.getContext(),"BAD clickity", Toast.LENGTH_SHORT).show();
                }

                TimeAndDate timeAndDate = TimeAndDate.getTimeAndDate();
                timeAndDate.setTimeToCustom(year, month, dayOfMonth, hour, minute);
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                isoFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID()));
                // Toast.makeText(App.getContext(), isoFormat.format(calendar.getDate()), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button reset = (Button) findViewById(R.id.reset_button);
        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                TimeAndDate timeAndDate = TimeAndDate.getTimeAndDate();
                timeAndDate.setTimeToCurrent();
                finish();
            }
        });

        Button selectTime = (Button) findViewById(R.id.set_time_button);
        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                if (calendar.getVisibility() == View.VISIBLE) {
                    timePicker.setVisibility(View.VISIBLE);
                    calendar.setVisibility(View.GONE);
                }
            }
        });

        Button selectDate = (Button) findViewById(R.id.set_day_button);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarView calendar = (CalendarView) findViewById(R.id.calendarView);
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                if (timePicker.getVisibility() == View.VISIBLE) {
                    calendar.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.GONE);
                }
            }
        });
    }
}
