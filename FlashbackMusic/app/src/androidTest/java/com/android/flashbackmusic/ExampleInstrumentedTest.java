package com.android.flashbackmusic;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

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
    public void writeUserAndReadInList() throws Exception {
        Song song = new Song(0,"titleName", "artistName", "AlbumName");
        songDao.insertSong(song);

        Song song1 = songDao.isIntheDB("titleName", "artistName", "AlbumName");
        assertEquals("titleName", song1.getTitle());


        song.setLastLocation(6);
        System.out.println(song.getLastLocation());
        songDao.updateSong(song);
        Song song2 = songDao.isIntheDB("titleName", "artistName", "AlbumName");


        System.out.println("TITLE:   "+song2.getTitle());
        System.out.println("ARTIST:  "+song2.getArtist());
        System.out.println("ALBUM:   "+song2.getAlbum());
        System.out.println("LASTLOC:   "+song2.getLastLocation());

        assertEquals(6, song2.getLastLocation());


        songDao.deleteSong(song);
        song1 = songDao.isIntheDB("as", "artistName", "AlbumName");
        assertTrue(null == song1);

    }
}
