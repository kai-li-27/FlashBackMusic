package com.android.flashbackmusic;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Kate on 2/4/2018. Allows the songs to actually play.
 */

public class SongsService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songIndex;
    private final IBinder musicBind = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    // starts playing the music
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void setList(ArrayList<Song> inSongs) {
        songs = inSongs;
    }

    public void onCreate() {
        super.onCreate();
        songIndex = 0;
        player = new MediaPlayer();

        initializeMusicPlayer();
    }

    public void setSong(int songPos) {
        songIndex = songPos;
    }

    public void playSong() {
        player.reset();
        Song songToPlay = songs.get(songIndex);
        long currSongId = songToPlay.getId();

        // FIXME
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSongId);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("SONGS SERVICE", "Error setting the data source", e);
        }

        player.prepareAsync();
    }

    public void initializeMusicPlayer() {
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

    //gets the song index
    public int getSongIndex(){return songIndex;}

    //gets me the song to play
    public Song getSong(int songIndex){return songs.get(songIndex);}

    public MediaPlayer getMediaPlayer(){return this.player;}


}
