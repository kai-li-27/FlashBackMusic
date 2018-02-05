package com.android.flashbackmusic;

/**
 * Created by Kate on 2/4/2018.
 */

public class Song {
    private long id;
    private String title;
    private String artist;
    private String lastLocation;
    private String album;
    // private Date thing

    public Song(long songId, String songTitle, String songArtist, String songAlbum) {
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        id = songId;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String location) {
        lastLocation = location;
    }

    // location method


}
