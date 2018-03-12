package com.android.flashbackmusic;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by kai on 3/5/2018.
 */
@RunWith(AndroidJUnit4.class)
public class SortingUnitTests {
    ArrayList<Song> songList;

    @Before
    public void initilaizeSongList() {
        songList  = SongManager.getSongManager().getDisplaySongList();
    }

    @Test
    public void testSortByTitle() {
        assertTrue(songList.size() > 0);
        SongManager.getSongManager().sortByTitle();
        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getTitle().compareTo(songList.get(i+1).getTitle()) <= 0);
        }
    }

    @Test
    public void testSortByMostRecent() {
        SongManager.getSongManager().sortByDefault();
        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getLastTime().compareTo(songList.get(i+1).getLastTime()) <= 0);
        }
    }

    @Test
    public void testSortByAlbum() {
        SongManager.getSongManager().sortByAlbum();
        String album = songList.get(0).getAlbum();
        for (int i = 0; i < songList.size() - 1; i++) {
            if (album.equals(songList.get(i + 1).getAlbum())) {
                assertTrue(songList.get(i).getTitle().compareTo(songList.get(i+1).getTitle()) <=0);
            } else {
                assertTrue(album.compareTo(songList.get(i+1).getAlbum()) < 0);
                album = songList.get(i+1).getAlbum();
            }
        }
    }

    @Test
    public void testSortByArtist() {
        SongManager.getSongManager().sortByArtist();
        String artist = songList.get(0).getArtist();
        for (int i = 0; i < songList.size() - 1; i++) {
            if (artist.equals(songList.get(i + 1).getArtist())) {
                assertTrue(songList.get(i).getTitle().compareTo(songList.get(i+1).getTitle()) <=0);
            } else {
                assertTrue(artist.compareTo(songList.get(i+1).getArtist()) < 0);
                artist = songList.get(i+1).getArtist();
            }
        }
    }


    @Test
    public void testSortByPreference() {
        SongManager.getSongManager().sortByFavorites();
        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getPreference() >= songList.get(i+1).getPreference());
        }
    }


}
