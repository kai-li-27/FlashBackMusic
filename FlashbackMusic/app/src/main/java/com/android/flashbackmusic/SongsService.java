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

    private ArrayList<Song> listOfAllSongs;
    private PriorityQueue<Song> flashBackPlayList;
    private ArrayList<Song> currentPlayList;

    private IndividualSong currentIndividualSong;
    private MainActivity mainActivity;
    private Song currentSong;
    private Location currlocation;
    private int currentIndex;
    private boolean flashBackMode = false;

    private boolean failedToGetLoactionPermission = true;//If you need to use this, ask Kai first
    private LocationManager locationManager;
    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();

    private static final String TAG = "SongsService";



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    /**
     * When a song is done playing, updates its fields, and play next song
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        // If not in flashbackmode, updates current playing song's fileds.
        Log.v(TAG, "song completed; updating fields");
        if (!flashBackMode) {
            currentSong = currentPlayList.get(currentIndex);
            currentSong.setLastTime(new Date(System.currentTimeMillis()));
            currentSong.setLastLocation(currlocation);
        }
        playNext();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
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

        flashBackPlayList = new PriorityQueue<Song>(new SongCompare());

        currentIndex = 0;
        player = new MediaPlayer();
        initializeMusicPlayer();
    }




    public void setList(ArrayList<Song> inSongs) {
        currentPlayList = inSongs;
    }

    public void setListOfAllSongs(ArrayList<Song> inList) {listOfAllSongs = inList;}

    public void setMainActivity(MainActivity mainActivity) { this.mainActivity = mainActivity;}

    public void setCurrentIndividualSong(IndividualSong individualSong) {
        currentIndividualSong = individualSong;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public Song getCurrentSong(){
        return currentSong;
    }

    public boolean getFlashBackMode() {
        return flashBackMode;
    }




    /**
     * Load the current playing song into the player
     */
    private void loadMedia() {
        Log.v(TAG, "loading current song into player");
        if (failedToGetLoactionPermission) { //Beucase the popup for asking for permision is asynchronous, we have to ckeck it again
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }


        // Not in flashback mode
        if (!flashBackMode) {
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
                Log.e(TAG, "Failed to load song!!");
            }
        }

        // In flashback mode
        else {

            // calculate the playlist
            for (Song i : listOfAllSongs) {
                i.updateDistance(currlocation);
                i.updateTimeDifference(new Date(System.currentTimeMillis()));
            }
            algorithm();

            if (flashBackPlayList.peek() == null) {
                Toast.makeText(SongsService.this, "FlashBack playlist is empty. Starting over.", Toast.LENGTH_SHORT).show();
                for (Song i : listOfAllSongs) {
                    i.setPlayed(false);
                }
                algorithm();
            }

            try {
                player.reset();
                currentSong = flashBackPlayList.peek();
                currentSong.setPlayed(true);
                player.setDataSource(getApplicationContext(), currentSong.uri);
                player.prepare();
            } catch (IOException e) {
                System.out.println("************************");
                System.out.println("Failed to load song!!!!!");
                System.out.println("************************");
                Log.e(TAG, "Failed to long song!!");
            }
        }

    }

    /**
     * Load the song at given index to the player
     */
    public void loadMedia(int index) {
        Log.v(TAG, "loadMedia; loading media into player");
        if (failedToGetLoactionPermission) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }

        if (flashBackMode) {
            loadMedia();
            return;
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
            Log.e(TAG, "Failed to load song!!");
        }

    }

    /**
     * Initialize the player to get some functionality
     */
    private void initializeMusicPlayer() {
        // let the music keep playing if it's already playing if it sleeps
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }




    /**
     * If the song's playing it, pause it, and vice versa
     */
    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    /**
     * Reset the current playing song, and pause the player
     */
    public void reset() {
        loadMedia();
    }

    /**
     * Play the next song, and change its info
     */
    public void playNext() {
        Log.v(TAG, "Playing the next song");
        if (!flashBackMode) {
            if (currentIndex < currentPlayList.size() - 1) { //Check if the end of playlist has been reached
                currentIndex++;
            } else {
                currentIndex = 0;
            }
        }

        loadMedia();
        player.start();

        if (currentIndividualSong != null) { //In case the app is at main screen now
            currentIndividualSong.changeText();
            currentIndividualSong.playPause();
        }
    }

    /**
     * Switch flashback mode
     */
    public void switchMode() {
        Log.i(TAG, "switchMode; toggling flashback mode");
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




    /**
     * In flashback mode, calculate the playableness of each song and add the playable song to playlist.
     */
    private void algorithm () {
        Log.v(TAG, "calculating playableness of each song");
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

            result = (1.0/distance)*2*distFactor + (1.0/timeDiff)*timeFactor + dayFactor;

            tempSong.setAlgorithmValue(result);

            if (result > 0 && !tempSong.isPlayed() && tempSong.getPreference() != Song.DISLIKE) {
                flashBackPlayList.add(tempSong);
            }
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

    public class MusicBinder extends Binder {
        SongsService getService() {
            return SongsService.this;
        }
    }
}
