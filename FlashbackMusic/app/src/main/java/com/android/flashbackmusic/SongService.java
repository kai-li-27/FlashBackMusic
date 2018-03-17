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
import android.net.Uri;
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
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * service that allows the songs to actually play with mediaplayer
 */

public class SongService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private enum Event {
        SONG_LOADED, SONG_COMPLETED, SONG_PAUSED, SONG_RESUMED, SONG_SKIPPED, VIBE_MODE_TOGGLED
    }
    private ArrayList<SongServiceEventListener> listeners = new ArrayList<>();

    private ArrayList<Song> currentPlayList;

    private Song emptySong;
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
        songManager.updateVibePlaylist(currlocation);
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
        getLocation();

        songManager = SongManager.getSongManager();
        currentPlayList = songManager.getCurrentPlayList();

        emptySong = new SongBuilder(Uri.parse(""), "empty","empty").setTitle("You don't have any songs")
                          .setAlbum("Try download some songs").setArtist("Playlist if empty").build();

        if (currentPlayList.size() > 0) { //IN case no songs have been downloaded
            currentSong = currentPlayList.get(0);
        } else {
            currentSong = emptySong;
        }
        player = new MediaPlayer();
        initializeMusicPlayer();

        /*
        SharedPreferences flashback_state = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
        if (flashback_state.getBoolean("State",false)) {
            switchMode(true);
        }
        */
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


        try {
            player.reset();
            player.setDataSource(getApplicationContext(), currentSong.getUri());
            player.prepare();
            notify(Event.SONG_LOADED);

        } catch (Exception e) {
            e.printStackTrace();
            notify(Event.SONG_LOADED);
            if (currentSong != emptySong) {
                Toast.makeText(App.getContext(), "This song is being downloaded", Toast.LENGTH_LONG).show();
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

        if (flashBackMode) { //for entering flashback on app start
            loadMedia();
            return;
        }

        try {
            player.reset();
            if (currentPlayList.size() > index) {
                currentSong = currentPlayList.get(index);
                player.setDataSource(getApplicationContext(), currentSong.getUri());
                player.prepare();
            } else {
                currentSong = new Song();
            }
            notify(Event.SONG_LOADED);
        } catch (IOException e) {
            e.printStackTrace();
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

        if (currentPlayList.size() == 0) {
            return;
        }

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
     */
    public void switchMode(boolean mode) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(App.getContext(), "You have to give me location permission to use me ^=^", Toast.LENGTH_LONG);
            return;
        }

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
        if (acct == null) {
            Toast.makeText(App.getContext(), "You must sign in before using the Vibe Mode!" , Toast.LENGTH_LONG).show();
            return;
        }

        if (currlocation == null) {
            getLocation();
            try {
                Thread.sleep(500);
            } catch (Exception e) {e.printStackTrace();}
            if (currlocation != null) {
                Toast.makeText(this, "Loading playlist. Come back later", Toast.LENGTH_LONG).show();
            }
            return;
        }





        Log.i(TAG, "switchMode; toggling flashback mode");
        if (flashBackMode && !mode) {
            flashBackMode = false;
            currentPlayList = songManager.getCurrentPlayList();
            if (currentPlayList.size() > 0) {
                currentSong = currentPlayList.get(0);
                loadMedia();
                player.start();
            } else {
                currentSong = emptySong;
                loadMedia();
            }
        }

        else if (!flashBackMode && mode) {
            currentPlayList = songManager.getVibeSongList();
            if (currentPlayList.size() == 0) {
                if (currlocation == null) {
                    Toast.makeText(App.getContext(), "Sorry! Unable to locate you", Toast.LENGTH_LONG).show();
                    currentPlayList = songManager.getCurrentPlayList();//Go back to normal mode
                    return;
                } else {
                    if (currentPlayList.size() == 0) {
                        Toast.makeText(App.getContext(), "No songs are found in this region! Be the one to write the history!!", Toast.LENGTH_LONG).show();
                        currentPlayList = songManager.getCurrentPlayList();//Go back to normal mode
                        return;
                    }
                }
            }

            flashBackMode = true;
            currentSong = currentPlayList.get(0);
            loadMedia();
            player.start();
        }
        notify(Event.VIBE_MODE_TOGGLED);

        SharedPreferences sharedPreferences = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("State", flashBackMode);
        editor.apply();
    }


    private void getLocation() {

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            currlocation = bestLocation;
            if (currlocation != null) {
                SongManager.getSongManager().updateVibePlaylist(currlocation);
            }
            failedToGetLoactionPermission = false;
        } catch (SecurityException e){}
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
