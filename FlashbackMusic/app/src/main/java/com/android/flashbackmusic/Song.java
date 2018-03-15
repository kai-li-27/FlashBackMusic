package com.android.flashbackmusic;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * holds information related to one song
 */

public class Song {

    public static final int NEUTRAL = 1;
    public static final int FAVORITE = 2;
    public static final int DISLIKE = 0;


    private String userIdString;
    private String email;
    private Uri uri;

    private String DownloadURL;
    private boolean IsPartOfAlbum = false;

    private String title = "No title";
    private String artist = "No artist";
    private String album = "No album";
    private String userDisplayName = "Anonymous"; //TODO give them cute name here. This is the default name

    private long lastTimeLong = 0;
    private int preference = NEUTRAL;

    private double lastLongitude = 0;
    private double lastLatitude = 0;
    private Date lastTime = null;

    private double distance = 0;
    private double timeDifference = 0;
    private double algorithmValue = 0;
    private boolean played = false;

    private static final String TAG = "Song";


//region Constructors
    public Song(Uri uri, String userIdString, String email) {
        if (uri == null || userIdString == null || email == null) {
            System.err.print("One of the parameter passed in to constructor of Song is null");
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.userIdString = userIdString;
        this.email = email;
        initializeLocationAndTime();
    }


    /**
     * NEVER CALL THIS. This is for firebase
     */
    public Song() {}
//endregion;





//region Getters
    public String getUserIdString(){
        return userIdString;
    }

    public String getEmail() {
        return email;
    }

    public String getDownloadURL() {
        return  DownloadURL;
    }

    public boolean isPartOfAlbum() {
        return IsPartOfAlbum;
    }

    @Exclude
    public Uri getUri() {
        return uri;
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

    @Exclude
    public String getUserDisplayName() {
        return userDisplayName;
    }

    @Exclude
    public Date getLastTime() {
        return new Date(lastTimeLong);
    }

    public int getPreference() {
        return preference;
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

    @Exclude // Stop the database from creating field for this
    public Location getLastLocation() {
        Location location = new Location("");
        location.setLatitude(lastLatitude);
        location.setLongitude(lastLongitude);
        return location;
    }

    @Exclude
    public double getDistance() {return distance;}

    @Exclude
    public double getTimeDifference() {return timeDifference;}

    @Exclude
    public double getAlgorithmValue() {
        return this.algorithmValue;
    }

    @Exclude
    public boolean isPlayed() {return played;}
//endregion;





//region Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserIdString(String userIdString) {
        this.userIdString = userIdString;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setDownloadURL(String downloadURL) {
        this.DownloadURL = downloadURL;
    }

    public void setIsPartOfAlbum(Boolean isPartOfAlbum) {
        this.IsPartOfAlbum = isPartOfAlbum;
    }

    public void setTitle(String title) { this.title = title;}

    public void setArtist(String artist) { this.artist = artist;}

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public void setLastLocation(Location location) {
        if (location != null) {
            lastLongitude = location.getLongitude();
            lastLatitude = location.getLatitude();
        }
    }

    public void setAlbum(String album) {this.album = album;}

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
        lastTimeLong = lastTime.getTime();
    }

    public void setPreference(int preference) {
        this.preference = preference;
    }

    public void rotatePreference() {
        preference = (preference + 1) % 3;
    }

    public void setLastTimeLong(long lastTimeLong) {
        this.lastTimeLong = lastTimeLong;
    }


    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public void setAlgorithmValue(double value) {
        this.algorithmValue = value;
    }
//endregion;





//region Real methods
    /**
     * Calculate the distance between given location to the location where it was played last time
     * @param here
     */
    public void updateDistance(Location here) {
        if (here == null) { // When distance is unavailable
            distance = 100000000; // Keep it from being played
        } else {
            distance = getLastLocation().distanceTo(here) * 3.28084; //Returns meter, convert to feet
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

        }
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
    /**
     * Fetch data from database and update the last time and last location it was played
     */
    private void initializeLocationAndTime() {
        //TODO fix it
    }


    @Exclude
    public String getDataBaseReferenceString() {
        return userIdString + ": " + title + ", " + album + ", " + artist; //TODO change User
    }
//endregion;

}
