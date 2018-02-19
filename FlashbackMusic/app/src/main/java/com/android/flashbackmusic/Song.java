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
        initializeLocationAndTime();
    }

    public Song() {
        title = "";
        artist = "";
        album = "";
    }

    /**
     * Fetch data from database and update the last time and last location it was played
     */
    private void initializeLocationAndTime() {
        preference = songDao.queryPreference(title,artist,album);
        lastTimeLong = songDao.queryLastTime(title,artist,album);
        if (lastTimeLong != 0) { // which means that the song was played before
            lastTime = new Date(lastTimeLong);

            lastLocation.setLatitude(songDao.queryLastLatitude(title,artist, album));
            lastLocation.setLongitude(songDao.queryLastLongitude(title,artist, album));
        } // else these two will be null
    }

    /**
     * Fetch title of song from database
     */
    public String getTitle() {
        return title;
    }

    /**
     * Fetch artist of song from database
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Fetch album of song from database
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Fetch last time when song was last played as a date
     */
    public Date getLastTime() {
        return lastTime;
    }

    /**
     * Fetch current preference of song
     */
    public int getPreference() {
        return preference;
    }

    /**
     * Fetch last location where song was played
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    /**
     * Fetch last time when song was played as a long
     */
    public long getLastTimeLong() {
        return lastTimeLong;
    }

    /**
     * Fetch the longitude of the location where a song was last played
     */
    public double getLastLongitude() {
        return lastLongitude;
    }

    /**
     * Fetch the latitude of the location where a song was last played
     */
    public double getLastLatitude() {
        return lastLatitude;
    }

    /**
     * Fetch distance of a song from database
     */
    public double getDistance() {return distance;}

    /**
     * Fetch time difference of a song from database
     */
    public double getTimeDifference() {return timeDifference;}

    /**
     * Fetch data from database to see if it is the same day
     */
    public boolean isSameDay() {return  isSameDay;}

    /**
     * Fetch data from database to see if it is the same time of day
     */
    public boolean isSameTimeOfDay() {return  isSameTimeOfDay;}

    /**
     * Fetch data from database to see if a song is played
     */
    public boolean isPlayed() {return played;}

    /**
     * Sets the title of a song, and update the data to database
     * @param title
     */
    public void setTitle(String title) { this.title = title;}

    /**
     * Sets the artist of a song, and update the data to database
     * @param artist
     */
    public void setArtist(String artist) { this.artist = artist;}

    /**
     * Set the location where it was played last, and update the data to database
     * @param location
     */
    public void setLastLocation(Location location) {
        if (location != null) {
            lastLocation = location;
            lastLongitude = location.getLongitude();
            lastLatitude = location.getLatitude();
            if (songDao != null) {
                songDao.updateSong(this);
            }
        }
    }

    /**
     * Set the album of a song, and update the data to database
     * @param album
     */
    public void setAlbum(String album) {this.album = album;}

    /**
     * Set the time where it was played last, and update the data to database
     * @param lastTime
     */
    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
        lastTimeLong = lastTime.getTime();
        if (songDao != null) {
            songDao.updateSong(this);
        }
    }

    /**
     * Set user-specified preference of this song, and update the data to database
     * @param preference
     */
    public void setPreference(int preference) {
        this.preference = preference;
        if (songDao != null){
            songDao.updateSong(this);
        }
    }

    /**
     * Rotate the preference as like the rotation of the button, and udpate the data to database
     */
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

    /**
     * Sets song for played status
     * @param played
     */
    public void setPlayed(boolean played) {
        this.played = played;
    }

    /**
     * Calculate the distance between given location to the location where it was played last time
     * @param here
     */
    public void updateDistance(Location here) {
        if (here == null) { // When distance is unavailable
            distance = 100000000; // Keep it from being played
        } else {
            distance = lastLocation.distanceTo(here) * 3.28084; //Returns meter, convert to feet
        }
    }

    /**
     * Given a time, determine if it is in same time range as the last time this song was played,
     * if it is in the same day of week, and how many minutes are between them.
     * @param now
     */
    public void updateTimeDifference(Date now) {
        if (lastTime == null) { // If the song was never played, then it would never appear on the list
            played = true;
        }

        else {
            // Get the time difference in minutes
            long difMiliseconds = Math.abs(now.getTime() - lastTimeLong);
            timeDifference = difMiliseconds / 1000 / 60 % (24 * 60);

            // If two songs are 10 mins apart, then practically they are in the same range
            if (timeDifference > 10) {
                if (timeRange(now.getHours()) == timeRange(lastTime.getHours())) {
                    isSameTimeOfDay = true;
                } else {
                    isSameTimeOfDay = false;
                }
            } else {
                isSameTimeOfDay = true;
            }

            // Determine if two time are same day of week
            if (now.getDay() == lastTime.getDay()) {
                isSameDay = true;
            } else {
                isSameDay = false;
            }

        }
    }

    /**
     * Sets algorithm value for this song
     * @param value
     */
    public void setAlgorithmValue(double value) {
        this.algorithmValue = value;
    }

    /**
     * Gets algorithm value for this song
     */
    public double getAlgorithmValue() {
        return this.algorithmValue;
    }

    /**
     * Given an hour, between 0-23, return its time range.
     * @param hour
     * @return
     */
    public int timeRange(int hour) {
        if (hour >= 5 && hour < 11) { //Morning
            return 0;
        } else if (hour >= 11 && hour < 17) { //Noon
            return 1;
        } else {
            return 2;
        }
    }

}
