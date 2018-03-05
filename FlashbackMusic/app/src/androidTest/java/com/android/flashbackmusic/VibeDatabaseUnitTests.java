package com.android.flashbackmusic;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by kai on 3/5/2018.
 */
@RunWith(AndroidJUnit4.class)
public class VibeDatabaseUnitTests {
    @Test
    public void testInsetIntoDataBase() {
        VibeDatabase database = new VibeDatabase();
        Song song = new Song("InsertTest","artistInsert","albumInsert",null);
        song.setUserIdString("UserInsertTest");
        database.insertSong(song);
        ArrayList<Song> list = database.querySongsOfUser("UserInsertTest");

        waitForServer();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getArtist().equals("artistInsert"));
        assertTrue(list.get(0).getAlbum().equals("albumInsert"));
    }

    @Test
    public void testUpdateIntoDataBase(){
        VibeDatabase database = new VibeDatabase();
        testInsetIntoDataBase();
        Song song = new Song("InsertTest","artistInsert","albumInsert",null);
        song.setUserIdString("UserInsertTest");
        song.setLastTimeLong(10);
        song.setLastLatitude(19);
        database.updateSong(song);
        ArrayList<Song> list = database.querySongsOfUser("UserInsertTest");

        waitForServer();
        assertEquals(1, list.size());
        assertTrue(list.get(0).getArtist().equals("artistInsert"));
        assertTrue(list.get(0).getAlbum().equals("albumInsert"));
        assertEquals(10, list.get(0).getLastTimeLong());
        assertEquals(19, list.get(0).getLastLatitude(), 0.1);
    }

    @Test
    public  void testQueryByLocationFromDatabase() {
        VibeDatabase database = new VibeDatabase();
        Song song = new Song("QueryTestNorthPole", "artist", "album", null);
        song.setLastLongitude(0);
        song.setLastLatitude(90); //NorthPole
        database.insertSong(song);
        Location location = new Location("");
        location.setLongitude(40); //at north pole longitude lines converge
        location.setLatitude(90);

        Song song2 = new Song("QueryTestSouthPole", "artist", "album", null);
        song2.setLastLongitude(0);
        song2.setLastLatitude(-90); //South Pole
        database.insertSong(song2);
        Location location2 = new Location("");
        location2.setLongitude(0);
        location2.setLatitude(-89.5);

        ArrayList<Song> list = database.queryByLocationOfAllSongs(location, 2);
        ArrayList<Song> list2 = database.queryByLocationOfAllSongs(location2, 200000);
        ArrayList<Song> list2A = database.queryByLocationOfAllSongs(location2, 184000);
        ArrayList<Song> list2B = database.queryByLocationOfAllSongs(location2, 150000);
        waitForServer();

        assertEquals(1, list.size());
        assertEquals(1, list2.size());
        assertEquals(1, list2A.size());
        assertEquals(0, list2B.size());
    }

    @Test
    public void testQueryByUserFromDatabase() {
        VibeDatabase database = new VibeDatabase();
        Song song = new Song("UserTest", "artist", "album", null);
        song.setUserIdString("user1");
        database.insertSong(song);
        ArrayList<Song> list1 = database.querySongsOfUser("user1");
        ArrayList<Song> list2 = database.querySongsOfUser("randomDude");
        waitForServer();
        assertEquals(1, list1.size());
        assertEquals(0, list2.size());

        assertTrue(list1.get(0).getTitle().equals("UserTest"));
        assertTrue(list1.get(0).getAlbum().equals("album"));
        assertTrue(list1.get(0).getArtist().equals("artist"));
    }

    @Test
    public void TestConnected() {
        VibeDatabase database = new VibeDatabase();
        waitForServer();
        assertTrue(database.isConnected());
    }

    private void waitForServer() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){}
    }

    //@Test
    public void TestForWhateverCrazyStuffs() {
        VibeDatabase databse = new VibeDatabase();
        databse.insertSong(null);
    }
}
