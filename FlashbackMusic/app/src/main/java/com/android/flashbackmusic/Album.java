package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by Kate and Camron on 2/7/2018.
 */

public class Album {
    private ArrayList<Song> songsInAlbum;
    private String name;
    private String artist;

    public Album(String name, String artist) {
        this.songsInAlbum = new ArrayList<Song>();
        this.name = name;
        this.artist = artist;
    }

    public ArrayList<Song> getSongsInAlbum() {
        return songsInAlbum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }
}
