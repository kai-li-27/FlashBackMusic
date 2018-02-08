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
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.net.URI;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // private MediaPlayer mediaPlayer;
    private static final int MEDIA_RES_ID = R.raw.jazz_in_paris;
    private ArrayList<Song> songsList;
    private ArrayList<Album> albumsList;
    private ListView songsView;
    private SongsService songsServ;
    private Intent playIntent;
    private boolean isMusicBound = false;
    SongListAdapter songAdapt;
    AlbumListAdapter albumAdapt;

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

    // method that is called once a song is clicked
    public void chosenSong(View view) {
        // songsServ.setSong(Integer.parseInt(view.getTag().toString()));
         //songsServ.playSong();
        Intent intent = new Intent(this, IndividualSong.class);
        intent.putExtra(Intent.EXTRA_TEXT,view.getTag().toString());
        startActivity(intent);

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
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;
            }
        }

        // FIXME for after MVP: get the last state instead of default songs list
        songsView = (ListView) findViewById(R.id.song_list);
        songsList = new ArrayList<Song>();
        albumsList = new ArrayList<Album>();
        getSongsList();
        // getAlbumsList(); FIXME, add me in after Kai and Eddy are done


        TabLayout tabLayout = (TabLayout) findViewById(R.id.topTabs);
        //TabItem songTab = (TabItem) findViewById(R.id.song_tab);
        //TabItem albumTab = (TabItem) findViewById(R.id.album_tab);

        // loadMedia(MEDIA_RES_ID); // load jazz in paris REMOVE LATER

        // could sort alphabetically for the songs

        songAdapt = new SongListAdapter(this, songsList);
        // albumAdapt = new AlbumListAdapter(this, albumsList);
        songsView.setAdapter(songAdapt);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String theTab = tab.getText().toString();
                if (theTab.equalsIgnoreCase("songs")) {
                    songsView.setAdapter(songAdapt);
                }
                if (theTab.equalsIgnoreCase("albums")) {
                    songsView.setAdapter(songAdapt); // FIXME-- will be albumAdapt later
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


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
