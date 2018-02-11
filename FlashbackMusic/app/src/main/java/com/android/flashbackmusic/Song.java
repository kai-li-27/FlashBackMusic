package com.android.flashbackmusic;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;

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
    private long lastTimeLong;
    private int preference = NEUTRAL;
    private double lastLongitude;
    private double lastAltitude;

    @Ignore
    public Uri uri;
    @Ignore
    private Location lastLocation = new Location("");
    @Ignore
    private long Id;
    @Ignore
    private Date lastTime;
    @Ignore
    private SongDao songDao;
    // private Date thing


    public Song(long Id, String title, String artist, String album, SongDao songDao) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.Id = Id;
        this.songDao = songDao;
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

    public Date getLastTime() {
        lastTimeLong = songDao.queryLastTime(title,artist,album);
        lastTime = new Date(lastTimeLong);
        return lastTime;
    }

    public int getPreference() {
        preference = songDao.queryPreference(title,artist,album);
        return preference;
    }

    public Location getLastLocation() {
        lastLongitude = songDao.queryLastLongitude(title,artist,album);
        lastAltitude = songDao.queryLastAltitude(title,artist,album);
        lastLocation.setLongitude(lastLongitude);
        lastLocation.setAltitude(lastLongitude);
        return lastLocation;
    }

    public long getId() {return Id;}

    public long getLastTimeLong() {
        return lastTimeLong;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public double getLastAltitude() {
        return lastAltitude;
    }

    public void setTitle(String title) { this.title = title;}

    public void setArtist(String artist) { this.artist = artist;}

    public void setLastLocation(Location location) {
        if (location != null) {
            lastLocation = location;
            lastLongitude = location.getLongitude();
            lastAltitude = location.getAltitude();
            songDao.updateSong(this);
        }
    }

    public void setAlbum(String album) {this.album = album;}

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
        lastTimeLong = lastTime.getTime();
        songDao.updateSong(this);
    }

    public void setId(int Id) {this.Id = Id;}

    public void setPreference(int preference) {
        this.preference = preference;
        songDao.updateSong(this);
    }

    public void rotatePreference() {
        preference = (preference + 1) % 3;
        songDao.updateSong(this);
    }

    public void setLastTimeLong(long lastTimeLong) {
    }

    public void setLastLongitude(double lastLongitude) {
    }

    public void setLastAltitude(double lastAltitude) {
    }
    // location method


}
