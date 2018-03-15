package com.android.flashbackmusic;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by soyel on 3/14/2018.
 */


public class SongBuilderUnitTest {

    SongBuilder songBuilder;
    Song song;
    Uri uri = new Uri.Builder().build() ;
    String username = "userIdString";
    String email = "email";
    String title = "title";
    String album = "album";
    String artist = "artist";
    long time = 101010101;
    double longitude = 420.69;
    double latitude = 1716.25;

    @Before
    public void initializeSong(){
        songBuilder = new SongBuilder(uri, username, email );
        song = songBuilder.getSong();
    }

    @Test
    public void testSongConstructor(){
        assert(song.getUri().equals(new Song(uri, username, email)));

    }

    @Test
    public void testSongUri(){
        assert(song.getUri().equals(uri));
    }

    @Test
    public void testSongUserIdString(){
        assert(song.getUserIdString().equals(username));
    }

    @Test
    public void testSongEmail(){
        assert(song.getEmail().equals(email));
    }

    @Test
    public void testSongBuilderArtist(){
        songBuilder.setArtist(artist);
        assert (song.getArtist().equals(artist));
    }

    @Test
    public void testSongBuilderAlbum(){
        songBuilder.setAlbum(album);
        assert(song.getAlbum().equals(album));
    }

    @Test
    public void testSongBuilderTitle(){
        songBuilder.setTitle(title);
        assert(song.getTitle().equals(title));
    }

    @Test
    public void testSongBuilderSetTime(){
        songBuilder.setLastTimeLong(time);
        assert(song.getLastTimeLong() == time);
    }

    @Test
    public void testSongBuilderSetLatitude(){
        songBuilder.setLastLatitude(latitude);
        assert (song.getLastLatitude() == latitude);
    }

    @Test
    public void testSongBuilderSetLongitude(){
        songBuilder.setLastLongitude(longitude);
        assert(song.getLastLongitude() == longitude);
    }
}
