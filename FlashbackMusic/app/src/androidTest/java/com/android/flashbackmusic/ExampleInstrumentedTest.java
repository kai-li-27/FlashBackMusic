package com.android.flashbackmusic;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private SongDao songDao;
    private SongDatabase Db;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        Db = Room.inMemoryDatabaseBuilder(context, SongDatabase.class).build();
        songDao = Db.songDao();
    }

    @After
    public void closeDb() throws IOException {
        Db.close();
    }

    @Test
    public void DataBaseOperationsTest() throws Exception {
        Song song = new Song("titleName", "artistName", "albumName",null);
        songDao.insertSong(song);

        Song song1 = songDao.isIntheDB("titleName", "artistName", "albumName");
        assertEquals("titleName", song1.getTitle());
        assertEquals("artistName", song1.getArtist());
        assertEquals("albumName", song1.getAlbum());
        assertEquals( 0, song1.getPreference());
        assertEquals( 0, song1.getLastTimeLong());
        assertEquals( 0, songDao.queryPreference("titleName", "artistName", "albumName"));
        assertEquals( 0, songDao.queryLastTime("titleName", "artistName", "albumName"));

        song.setLastTimeLong(6);
        songDao.updateSong(song);
        Song song2 = songDao.isIntheDB("titleName", "artistName", "albumName");
        assertEquals(6, song2.getLastTimeLong());
        assertEquals( 6, songDao.queryLastTime("titleName", "artistName", "albumName"));

        songDao.deleteSong(song);
        song1 = songDao.isIntheDB("as", "artistName", "albumName");
        assertTrue(null == song1);
    }


    @Rule
    public ActivityTestRule<MainActivity> mainactivity = new ActivityTestRule<MainActivity>(MainActivity.class);




}
