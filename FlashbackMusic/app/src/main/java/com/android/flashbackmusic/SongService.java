package com.android.flashbackmusic;

import android.*;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;

/**
 * service that allows the songs to actually play with mediaplayer
 */

public class SongService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private enum Event {
        SONG_LOADED, SONG_COMPLETED, SONG_PAUSED, SONG_RESUMED, SONG_SKIPPED, VIBE_MODE_TOGGLED
    }
    private ArrayList<SongServiceEventListener> listeners = new ArrayList<>();

    private ArrayList<Song> currentPlayList;

    private Song currentSong;
    private Location currlocation = null;
    private boolean flashBackMode = false;
    private SongManager songManager;

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
            TimeAndDate instance = TimeAndDate.getTimeAndDate();
            if(!instance.isTimeCurrentTime()){
                currentSong.setLastTime(new Date(instance.getDateSelected()));
            }
            else {
                currentSong.setLastTime(new Date(System.currentTimeMillis()));
            }
            currentSong.setLastLocation(currlocation);
            VibeDatabase.getDatabase().updateSong(currentSong);
        }
        notify(Event.SONG_COMPLETED);
        int currentIndex = currentPlayList.indexOf(currentSong);
        if (currentIndex == currentPlayList.size() - 1) {
            currentIndex = 0;
            Toast.makeText(App.getContext(), "Reached the end of playlist. Starting over.", Toast.LENGTH_LONG).show();
        } else {
            currentIndex++;
        }
        currentSong = currentPlayList.get(currentIndex);
        loadMedia();
        player.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
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

        songManager = SongManager.getSongManager();
        currentPlayList = songManager.getCurrentPlayList();


        if (currentPlayList.size() > 0) { //IN case no songs have been downloaded
            currentSong = currentPlayList.get(0);
        }
        player = new MediaPlayer();
        initializeMusicPlayer();

        SharedPreferences flashback_state = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
        if (flashback_state.getBoolean("State",false)) {
            switchMode(true);
        }
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
     * @precondition currentSong is set to the song to be loaded
     */
    private void loadMedia() {
        Log.v(TAG, "loading current song into player");
        if (failedToGetLoactionPermission) { //Beucase the popup for asking for permision is asynchronous, we have to ckeck it again
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
                failedToGetLoactionPermission = false;
            } catch (SecurityException e) {}
        }


        try {
            player.reset();
            player.setDataSource(getApplicationContext(), currentSong.getUri());
            player.prepare();
            notify(Event.SONG_LOADED);

        } catch (Exception e) {
            System.out.println("************************");
            System.out.println("Failed to load song!!!!!");
            System.out.println("************************");
            Log.e(TAG, "Failed to load song!!");
            Toast.makeText(App.getContext(), "This song wasn't downloaded", Toast.LENGTH_LONG).show();
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
            currentSong = currentPlayList.get(index);

            player.setDataSource(getApplicationContext(), currentSong.getUri());
            player.prepare();
            notify(Event.SONG_LOADED);
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
            notify(Event.SONG_PAUSED);
        } else {
            player.start();
            notify(Event.SONG_RESUMED);
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
        int currentIndex = currentPlayList.indexOf(currentSong);
        if (currentIndex == currentPlayList.size()-1) {
            currentIndex = 0;
            Toast.makeText(App.getContext(), "Reached the end of playlist. Starting over.", Toast.LENGTH_LONG).show();
        } else {
            currentIndex++;
        }
        currentSong = currentPlayList.get(currentIndex);
        loadMedia();
        player.start();
        notify(Event.SONG_SKIPPED);
    }

    /**
     * Switch flashback mode
     * TODO make sure user is logged in to google acc, connected to internet,
     */
    public void switchMode(boolean mode) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());

        if (acct == null) {
            Toast.makeText(App.getContext(), "You must sign in before using the Vibe Mode!" , Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(App.getContext(), "You have to give me location permission to use me ^=^", Toast.LENGTH_LONG);
            //TODO if have time, request permission here
            return;
        }

        Log.i(TAG, "switchMode; toggling flashback mode");
        if (flashBackMode && !mode) {
            flashBackMode = false;
        }  else if (!flashBackMode && mode) {
            flashBackMode = true;
            currentPlayList = songManager.getVibeSongList();
            System.out.println(currentPlayList.size());
            if (currentPlayList.size() == 0) {
                try {
                    Thread.sleep(2000); //give the app some time to load data
                } catch (Exception e){} //TODO change this to progress bar so that the app is not freezed
                if (currentPlayList.size() == 0) {
                    if (currlocation == null) {
                        Toast.makeText(App.getContext(), "Sorry! Unable to locate you", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(App.getContext(), "No songs are found in this region! Be the one to write the history!!", Toast.LENGTH_LONG).show();
                    }
                    flashBackMode = false;
                    currentPlayList = songManager.getCurrentPlayList();//Go back to normal mode
                    return;
                }
            }
            currentSong = currentPlayList.get(0);
            System.out.println(currentSong.getUri());
            loadMedia();
            player.start();
        }
        notify(Event.VIBE_MODE_TOGGLED);

        SharedPreferences sharedPreferences = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("State", flashBackMode);
        editor.apply();
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
            if (currlocation == null) {
                currlocation = location;
                SongManager.getSongManager().updateVibePlaylist(location);
                return;
            }
            if (location.distanceTo(currlocation) * 3.28 > 1000) { //feet
                currlocation = location;
                SongManager.getSongManager().updateVibePlaylist(location);
            }
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
        SongService getService() {
            return SongService.this;
        }
    }





//region Listeners
    public void addSongServiceEventListener(SongServiceEventListener listener) {
        listeners.add(listener);
    }

    public void removeSongServiceEventListener(SongServiceEventListener listener) {
        listeners.remove(listener);
    }

    public void notify(Event event) {
        for (SongServiceEventListener i : listeners) {
            switch (event) {
                case SONG_LOADED:
                    i.onSongLoaded(currentSong);
                    break;
                case SONG_COMPLETED:
                    i.onSongCompleted(currentSong,currentSong);//TODO
                    break;
                case SONG_PAUSED:
                    i.onSongPaused(currentSong);
                    break;
                case SONG_RESUMED:
                    i.onSongResumed(currentSong);
                    break;
                case SONG_SKIPPED:
                    i.onSongSkipped(currentSong,currentSong);//TODO
                    break;
                case VIBE_MODE_TOGGLED:
                    i.onVibeModeToggled(flashBackMode);
                    break;
            }
        }
    }
//endregion;
}
