package com.android.flashbackmusic;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kate on 2/4/2018. Allows the songs to actually play.
 */

public class SongsService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private Song currentSong;
    private MediaPlayer player;
    private ArrayList<Song> songsList;
    private int currentIndex;
    private final IBinder musicBind = new MusicBinder();
    private LocationManager locationManager;
    private IndividualSong currentIndividualSong;
    private SongDao songDao;
    private Location currlocation;
    private boolean reseted = false;
    private boolean failedToGetLoactionPermission = true;//If you need to use this, ask Kai first



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (currentIndex < songsList.size() - 1) { //Check if the end of playlist has been reached
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        loadMedia();
        if (currentIndividualSong != null) { //In case the app is at main screen now
            currentIndividualSong.changeText();
        }
        //TODO tell the individualsong to update info
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    // starts playing the music
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!reseted) {
            mp.start();
        } else {
            reseted = false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SongDatabase Db = SongDatabase.getSongDatabase(getApplicationContext()); // Load database
        //update song's Lasttime to current time
        // date = Calendar.getInstance().getTime();
        //currentSong.setLastTime(date.getTime());

        // Todo update song's lastLocation to current location



        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
            failedToGetLoactionPermission = false;
        } catch (SecurityException e){}
        songDao = Db.songDao();
        currentIndex = 0;
        player = new MediaPlayer();
        initializeMusicPlayer();
    }


    public void setList(ArrayList<Song> inSongs) {
        songsList = inSongs;
    }

    /**
     * This method is intened to be only used by within the class
     */
    private void loadMedia() {
        if (failedToGetLoactionPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }
        try {
            player.reset();
            currentSong = songsList.get(currentIndex);
            currentSong.setLastLocation(currlocation);
            player.setDataSource(getApplicationContext(), currentSong.uri);
            player.prepare();

        } catch (IOException e) {
            System.out.println("************************");
            System.out.println("Failed loading song!!!!!");
            System.out.println("************************");
        }
    }

    public void loadMedia(int index) {
        if (failedToGetLoactionPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }
        try {
            currentIndex = index;
            currentSong = songsList.get(index);
            if (currlocation != null) {
                currentSong.setLastLocation(currlocation);
            }
            player.reset();
            player.setDataSource(getApplicationContext(), songsList.get(currentIndex).uri);
            player.prepare();
        } catch (IOException e) {
            System.out.println("************************");
            System.out.println("Failed loading song!!!!!");
            System.out.println("************************");
        }
    }

    public void reset() {
        loadMedia(); //This will start playing the song
        reseted = true;
    }


    private void initializeMusicPlayer() {
        // let the music keep playing if it's already playing if it sleeps
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    // for interaction between the activity and SongsService
    public class MusicBinder extends Binder {
        SongsService getService() {
            return SongsService.this;
        }
    }

    public void skip() {
        if (currentIndex < songsList.size() - 1) { //Check if the end of playlist has been reached
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        loadMedia();
    }

    // Get a reference to IndivudalSong to update song on completion
    public void setCurrentIndividualSong(IndividualSong individualSong){
        currentIndividualSong = individualSong;
    }

    //gets the song index
    public int getSongIndex(){
        return currentIndex;
    }

    public Song getCurrentSong(){
        return currentSong;
    }

    public MediaPlayer getMediaPlayer(){
        return player;
    }

    private final LocationListener mLocationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("I am called !!!!!!!!!");
            currlocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

}
