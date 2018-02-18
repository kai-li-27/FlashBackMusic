package com.android.flashbackmusic;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;

/**
 * Created by Kate on 2/4/2018. Allows the songs to actually play.
 */

public class SongsService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private Song currentSong;
    private Comparator<Song>  comparator = new SongCompare();
    private MediaPlayer player;
    private ArrayList<Song> listOfAllSongs;
    private PriorityQueue<Song> flashBackPlayList = new PriorityQueue<Song>(comparator);
    private ArrayList<Song> currentPlayList;
    private int currentIndex;
    private final IBinder musicBind = new MusicBinder();
    private LocationManager locationManager;
    private IndividualSong currentIndividualSong;
    private MainActivity mainActivity;
    private Location currlocation;
    private boolean flashBackMode = false;

    private boolean reseted = false; //The reset button was pressed
    private boolean failedToGetLoactionPermission = true;//If you need to use this, ask Kai first



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!flashBackMode) {
            if (currentIndex < currentPlayList.size() - 1) { //Check if the end of playlist has been reached
                currentIndex++;
            } else {
                currentIndex = 0;
            }
        }
        if (flashBackMode) {
            for (Song i : listOfAllSongs) {
                i.updateDistance(currlocation);
                i.updateTimeDifference(new Date(System.currentTimeMillis()));
                algorithm();
            }
        }
        loadMedia();
        if (currentIndividualSong != null) { //In case the app is at main screen now
            currentIndividualSong.changeText();
        }
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

        if (currentIndividualSong != null) {
            currentIndividualSong.playPause(); // In case skip button was pressed when the player in paused
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize location tracker
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
            failedToGetLoactionPermission = false;
        } catch (SecurityException e){}

        currentIndex = 0;
        player = new MediaPlayer();
        initializeMusicPlayer();
    }



    public void setList(ArrayList<Song> inSongs) {
        currentPlayList = inSongs;
    }
    public void setListOfAllSongs(ArrayList<Song> inList) {listOfAllSongs = inList;}
    public void setMainActivity(MainActivity mainActivity){ this.mainActivity = mainActivity;}

    /**
     * This method is intened to be only used by within the class
     */
    private void loadMedia() {
        if (failedToGetLoactionPermission) { //Beucase the popup for asking for permision is asynchronous, we have to ckeck it again
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }


        if (!flashBackMode) { //Not in flashback mode, update time and location
            try {
                player.reset();
                currentSong = currentPlayList.get(currentIndex);
                currentSong.setLastLocation(currlocation);
                currentSong.setLastTime(new Date(System.currentTimeMillis()));
                player.setDataSource(getApplicationContext(), currentSong.uri);
                player.prepare();
            } catch (IOException e) {
                System.out.println("************************");
                System.out.println("Failed to load song!!!!!");
                System.out.println("************************");
            }
        } else {
            try {
                player.reset();
                if (flashBackPlayList.peek() == null) {
                    Toast.makeText(SongsService.this, "FlashBack Playlist Is Empty", Toast.LENGTH_LONG).show();
                    return;
                }
                currentSong = flashBackPlayList.peek();
                currentSong.setPlayed(true);
                player.setDataSource(getApplicationContext(), currentSong.uri);
                player.prepare();
            } catch (IOException e) {
                System.out.println("************************");
                System.out.println("Failed to load song!!!!!");
                System.out.println("************************");
            }
        }
    }



    /*
     * Load the song at index of all the songs.
     * WARNING: This should not be called in flashback mode!!!
     */
    public void loadMedia(int index) {
        if (failedToGetLoactionPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }

        try {
            player.reset();
            currentIndex = index;
            currentSong = currentPlayList.get(index);
            if (!flashBackMode) {
                currentSong.setLastLocation(currlocation);
                currentSong.setLastTime(new Date(System.currentTimeMillis()));
            }

            player.setDataSource(getApplicationContext(), currentSong.uri);
            player.prepare();
        } catch (IOException e) {
            System.out.println("************************");
            System.out.println("Failed to load song!!!!!");
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
        onCompletion(player);
        loadMedia();
    }

    // Get a reference to IndivudalSong to update song on completion
    public void setCurrentIndividualSong(IndividualSong individualSong){
        currentIndividualSong = individualSong;
    }


    public Song getCurrentSong(){
        return currentSong;
    }

    public MediaPlayer getMediaPlayer(){
        return player;
    }

    public boolean getFlashBackMode() {
        return flashBackMode;
    }

    public void switchMode() {
        if (flashBackMode) {
            flashBackMode = false;
        } else {
            flashBackMode = true;
            for ( Song i : listOfAllSongs ) { //Set all the songs to not played
                i.setPlayed(false);
            }
        }
        mainActivity.changeBackgroundForFlashback();

        SharedPreferences sharedPreferences = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("State", flashBackMode);
        editor.apply();
    }

    private final LocationListener mLocationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
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


    private void algorithm () {
        double distFactor = 1.0;
        double timeFactor = 1.0;
        double dayFactor = 1.0;
        double result = 0.0;
        double distance, timeDiff;
        boolean sameTime, sameDay;
        Song tempSong;
        flashBackPlayList.clear();

        for (int i = 0; i < listOfAllSongs.size(); i++) {
            tempSong = listOfAllSongs.get(i);
            distance = tempSong.getDistance();
            sameTime = tempSong.isSameTimeOfDay();
            sameDay = tempSong.isSameDay();
            timeDiff = tempSong.getTimeDifference();
            distFactor = 1.0;
            timeFactor = 1.0;
            dayFactor = 1.0;

            // TODO Change back to 1000 later
            if (distance > 1000) {
                distFactor = 0.0;
            }
            if (!sameTime) {
                timeFactor = 0.0;
            }
            if (!sameDay) {
                dayFactor = 0.0;
            }

            if (distance < 1) {
                distance = 1;
            }

            if( timeDiff < 1) {
                timeDiff = 1;
            }

            //if ( distance ) //TODO cornor cases for
            result = (1.0/distance)*2*distFactor + (1.0/timeDiff)*timeFactor + dayFactor;

            tempSong.setAlgorithmValue(result);

            if (result > 0 && !tempSong.isPlayed() && tempSong.getPreference() != Song.DISLIKE) {
                flashBackPlayList.add(tempSong);
            }
            System.out.println(result);
        }
    }

    private class SongCompare implements Comparator<Song> {
        public int compare(Song s1, Song s2) {
            if ( (s1.getAlgorithmValue() - s2.getAlgorithmValue()) == 0 ) {
                if ( s1.getPreference() == s2.getPreference()) {
                    return 0;
                }
                else if ( s1.getPreference() == Song.FAVORITE) {
                    return 1;
                } else {
                    return  -1;
                }
            }
            else if ((s1.getAlgorithmValue() - s2.getAlgorithmValue()) > 0) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }

}
