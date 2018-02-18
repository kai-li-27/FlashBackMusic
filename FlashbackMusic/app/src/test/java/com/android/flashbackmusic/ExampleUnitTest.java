package com.android.flashbackmusic;

import android.location.Location;

import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void testCalculateTime(){
        Song song = new Song("", "", "", null);

        song.setLastTime(new Date(System.currentTimeMillis()));
        song.updateTimeDifference(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)); //add a day
        assertTrue(song.isSameTimeOfDay());
        assertFalse(song.isSameDay());
        assertEquals(0, song.getTimeDifference(), 0.1);

        song.updateTimeDifference(new Date((System.currentTimeMillis()) + 1000 * 60 * 60 * 12)); // add 12 hours
        assertFalse(song.isSameDay());
        assertFalse(song.isSameTimeOfDay());
        assertEquals(720, song.getTimeDifference(), 0.1);

        song.updateTimeDifference(new Date((System.currentTimeMillis()) + 1000 * 60 * 60 * 6)); // add 6 hours
        assertFalse(song.isSameDay()); // It's 6:36 pm at time of testing
        assertTrue(song.isSameTimeOfDay());
        assertEquals(360, song.getTimeDifference(), 0.1);

        song.updateTimeDifference(new Date((System.currentTimeMillis()) + 1000 * 60 * 60)); // add 1 hours
        assertTrue(song.isSameDay()); // It's 6:36 pm at time of testing
        assertTrue(song.isSameTimeOfDay());
        assertEquals(60, song.getTimeDifference(), 0.1);

        song.updateTimeDifference(new Date((System.currentTimeMillis()) + 1000 * 60 * 60 * 10)); // add 10 hours
        assertFalse(song.isSameDay()); // It's 7:01 pm at time of testing
        assertFalse(song.isSameTimeOfDay());
        assertEquals(600, song.getTimeDifference(), 0.1);

        song.updateTimeDifference(new Date((System.currentTimeMillis()) + 1000 * 60 * 34)); // add 34 minutes
        assertEquals(34, song.getTimeDifference(), 0.1);
    }

    @Test
    public void TestUpdateLocation() {
    }

}