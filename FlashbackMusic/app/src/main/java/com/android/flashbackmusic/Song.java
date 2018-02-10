package com.android.flashbackmusic;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by Kate on 2/4/2018.
 */

@Entity(primaryKeys = {"title", "artist", "album"})
public class Song {

    public static final int NEUTRAL = 0;
    public static final int FAVORITE = 1;
    public static final int DISLIKE = 2;

    @NonNull
    private String title;
    @NonNull
    private String artist;
    @NonNull
    private String album;
    private int lastLocation;
    private long lastTime;

    @Ignore
    private long Id;
    private int preference = NEUTRAL;
    // private Date thing


    public Song(long Id, String title, String artist, String album) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.Id = Id;
    }

    public Song(){
        title = "";
        artist = "";
        album = "";
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

    public long getLastTime() {return lastTime;}

    public int getPreference() {return preference;}

    public int getLastLocation() {
        return lastLocation;
    }

    public long getId() {return Id;}

    public void setTitle(String title) { this.title = title;}

    public void setArtist(String artist) { this.artist = artist;}

    public void setLastLocation(int location) {
        lastLocation = location;
    }

    public void setAlbum(String album) {this.album = album;}

    public void setLastTime(long lastTime) {this.lastTime = lastTime;}

    public void setId(int Id) {this.Id = Id;}

    public void setPreference(int preference) {this.preference = preference;}

    public void rotatePreference() {
        preference = (preference + 1) % 3;
    }

    // location method


}
