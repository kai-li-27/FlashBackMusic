package com.android.flashbackmusic;

import android.net.Uri;

import java.util.Date;

/**
 * Created by soyel on 3/7/2018.
 */

public class SongBuilder {

//    private String userIdString;
//    private String email;
//    private Uri uri;
//    private String title = "No Title";
//    private String artist = "No Artist" ;
//    private String album = "no Album";
//    private long lastTimeLong = 0;
//    private double lastLongitude = 0;
//    private double lastLatitude = 0;
//
//    private Date lastTime= null;
//    private double distance = 0;
//    private double timeDifference = 0;
//    private double algorithmValue = 0;
//    private static final String TAG = "Song";
    private Song song;

    public SongBuilder(Uri uri, String userIdString, String email){
        song = new Song(uri,  userIdString, email);
    }

    public Song build(){
        return song;
    }
    public SongBuilder setTitle(final String title1){
        song.setTitle(title1);
        return this;
    }

    public SongBuilder setArtist(final String artist1){
        song.setArtist(artist1);
        return this;
    }

    public SongBuilder setAlbum(final String album1){
        song.setAlbum(album1);
        return this;
    }

    public SongBuilder setLastTimeLong(final long lastTime){
        song.setLastTimeLong(lastTime);
        return this;
    }

    public SongBuilder setLastLongitude(final double lastLongitude1){
        song.setLastLongitude(lastLongitude1);
        return this;
    }

    public SongBuilder setLastLatitude(final double lastLatitude1){
        song.setLastLatitude(lastLatitude1);
        return this;
    }

    public SongBuilder setDownLoadURL(String url) {
        song.setDownloadURL(url);
        return this;
    }

    public SongBuilder setPartOfAlbum(boolean is) {
        song.setIsPartOfAlbum(is);
        return  this;
    }



}
