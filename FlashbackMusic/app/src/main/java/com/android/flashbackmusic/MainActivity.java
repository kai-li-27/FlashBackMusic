package com.android.flashbackmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // private MediaPlayer mediaPlayer;
    private static final int MEDIA_RES_ID = R.raw.jazz_in_paris;
    private ArrayList<Song> songsList;
    private ListView songsView;
    private SongsService songsServ;
    private Intent playIntent;
    private boolean isMusicBound = false;

    /*
    public void loadMedia(int resourceId) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    } */

    public void chosenSong(View view) {
        songsServ.setSong(Integer.parseInt(view.getTag().toString()));
        songsServ.playSong();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, SongsService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        /*
        if (isChangingConfigurations() && mediaPlayer.isPlaying()) {
            ; //"do nothing"
        }
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        songsServ = null;
        stopService(playIntent);
        // mediaPlayer.release();
    }

    public void getSongsList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            // TODO grabbing album title
            do {
                long currId = musicCursor.getLong(idColumn);
                String currTitle = musicCursor.getString(titleColumn);
                String currArtist = musicCursor.getString(artistColumn);
                // TODO same for album title
                songsList.add(new Song(currId, currTitle, currArtist, "beep")); // FIXME album input
            } while (musicCursor.moveToNext());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }
        }

        songsView = (ListView) findViewById(R.id.song_list);
        songsList = new ArrayList<Song>();
        getSongsList();
        // loadMedia(MEDIA_RES_ID); // load jazz in paris REMOVE LATER

        // could sort alphabetically for the songs


        SongListAdapter songAdapt = new SongListAdapter(this, songsList);
        songsView.setAdapter(songAdapt);

        /*
        // toggle between play and pause
        Button playButton = (Button) findViewById(R.id.button_play);
        playButton.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { /*
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }*/
                /*
                    }
                }); */ /*

        Button resetButton = (Button) findViewById(R.id.button_reset);
        resetButton.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { /*
                        mediaPlayer.reset();
                        loadMedia(MEDIA_RES_ID);*/
                    /*}
                }); */
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongsService.MusicBinder binder = (SongsService.MusicBinder)service;
            songsServ = binder.getService();
            songsServ.setList(songsList);
            isMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };

}
