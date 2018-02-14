package com.android.flashbackmusic;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.sql.Time;
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
    private double lastLatitude;

    @Ignore
    public Uri uri;
    @Ignore
    private Location lastLocation = new Location("");
    @Ignore
    private Date lastTime;
    @Ignore
    private SongDao songDao;
    @Ignore
    private double distance;
    @Ignore
    private double timeDifference;
    @Ignore
    private boolean isSameDay;
    @Ignore
    private boolean isSameTimeOfDay;
    @Ignore
    private double algorithmValue;
    @Ignore
    private boolean played = false;



    public Song(String title, String artist, String album, SongDao songDao) {
        this.title = title;
        this.artist = artist;
        this.album = album;
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
        if (lastTimeLong != 0) { // which means that the song was played before
            lastTime = new Date(lastTimeLong);
        }
        return lastTime;
    }

    public int getPreference() {
        return preference;
    }

    public Location getLastLocation() {
        lastLocation.setLatitude(songDao.queryLastLatitude(title,artist, album)); //TODO moved to the somewhere
        lastLocation.setLongitude(songDao.queryLastLongitude(title,artist, album));
        return lastLocation;
    }


    public long getLastTimeLong() {
        return lastTimeLong;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    public double getDistance() {return distance;}

    public double getTimeDifference() {return timeDifference;}

    public boolean isSameDay() {return  isSameDay;}

    public boolean isSameTimeOfDay() {return  isSameTimeOfDay;}

    public boolean isPlayed() {return played;}

    public void setTitle(String title) { this.title = title;}

    public void setArtist(String artist) { this.artist = artist;}

    public void setLastLocation(Location location) {
        if (location != null) {
            lastLocation = location;
            lastLongitude = location.getLongitude();
            lastLatitude = location.getLatitude();
            if (songDao != null) { // TODO remove this condition. This condition is only for testing
                songDao.updateSong(this);
            }
        }
    }

    public void setAlbum(String album) {this.album = album;}

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
        lastTimeLong = lastTime.getTime();
        if (songDao != null) { // TODO remove this condition. this condition is only for testing
            songDao.updateSong(this);
        }
    }

    public void setPreference(int preference) {
        this.preference = preference;
        if (songDao != null){
            songDao.updateSong(this);
        }
    }

    public void rotatePreference() {
        preference = (preference + 1) % 3;
        songDao.updateSong(this);
    }

    public void setLastTimeLong(long lastTimeLong) {
    }

    public void setLastLongitude(double lastLongitude) {
    }

    public void setLastLatitude(double lastLatitude) {
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public void updateDistance(Location here) {
        getLastLocation();
        distance = lastLocation.distanceTo(here) * 3.28084; //Returns meter, convert to feet
    }

    public void updateTimeDifference(Date now) {
        getLastTime();
        if (lastTime == null) {
            played = true;
        } else {
            if (now.getDay() == lastTime.getDay()) { //Todo 5 mintues before midnight
                isSameDay = true;
            } else {
                isSameDay = false;
            }

            long difMiliseconds = Math.abs(now.getTime() - lastTimeLong);
            timeDifference = difMiliseconds / 1000 / 60 % (24 * 60);


            // location method

            if (timeRange(now.getHours()) == timeRange(lastTime.getHours())) {
                isSameTimeOfDay = true;
            } else {
                isSameTimeOfDay = false;
            }
        }
    }

    public void setAlgorithmValue(double value) {
        this.algorithmValue = value;
    }

    public double getAlgorithmValue() {
        return this.algorithmValue;
    }

    public int timeRange(int hour) {
        if (hour >= 5 && hour < 11) { //Morning
            return 0;
        } else if (hour >= 11 && hour < 5) {
            return 1;
        } else {
            return 2;
        }
    }

}
