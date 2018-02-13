package com.android.flashbackmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.flashbackmusic.SongsService.MusicBinder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> listOfAllSongs = new ArrayList<Song>();
    private ArrayList<Song> currentPlayList = new ArrayList<Song>();
    private HashMap<String, Album> albumsMap = new HashMap<String, Album>();
    private SongsService songsService;
    private ArrayList<Album> albumsList;
    private ListView songsView;
    private Intent playIntent;
    private boolean didChooseAlbum = false;
    Album currAlbum;
    private boolean isMusicBound = false;
    SongListAdapter songAdapt;
    AlbumListAdapter albumAdapt;

    private SongDao songDao;

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, SongsService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    //TODO release the music player
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
        stopService(playIntent);
        // mediaPlayer.release();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }

        // Load database
        SongDatabase Db = SongDatabase.getSongDatabase(getApplicationContext());
        songDao = Db.songDao();

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        */

        // TODO Iteration 2: get the last state instead of default songs list
        songsView = (ListView) findViewById(R.id.song_list);
        listOfAllSongs = new ArrayList<Song>();
        currentPlayList = new ArrayList<Song>();
        albumsList = new ArrayList<Album>();
        getSongsList();
        getAlbumList();


        TabLayout tabLayout = (TabLayout) findViewById(R.id.topTabs);
        //TabItem songTab = (TabItem) findViewById(R.id.song_tab);
        //TabItem albumTab = (TabItem) findViewById(R.id.album_tab);

        songAdapt = new SongListAdapter(this, listOfAllSongs);
        albumAdapt = new AlbumListAdapter(this, albumsList);
        songsView.setAdapter(songAdapt);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String theTab = tab.getText().toString();
                if (theTab.equalsIgnoreCase("songs")) {
                    songsView.setAdapter(songAdapt);
                }
                if (theTab.equalsIgnoreCase("albums")) {
                    songsView.setAdapter(albumAdapt);
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


    // method that is called once a song is clicked
    public void chosenSong(View view) {
        Intent intent = new Intent(this, IndividualSong.class);
        intent.putExtra(Intent.EXTRA_INDEX,(int)view.getTag()); //view.getTage() returns the index of the song in the displayed list
        if (didChooseAlbum) {
            currentPlayList.clear();
            for (Song i : listOfAllSongs) {
                currentPlayList.add(i);
            }
            didChooseAlbum = false;
        }
        startActivity(intent);
    }

    public void chosenAlbum(View view) {
        Intent intent = new Intent(this, IndividualSong.class);
        currAlbum = albumsList.get((int)view.getTag());
        didChooseAlbum = true;
        currentPlayList.clear();
        for (Song i : currAlbum.getSongsInAlbum()) {
            currentPlayList.add(i);
        }
        intent.putExtra(Intent.EXTRA_INDEX, 0); //0 means to play the first song
        startActivity(intent);
    }


    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            songsService = binder.getService();
            if (didChooseAlbum) {
                songsService.setList(currAlbum.getSongsInAlbum());
            } else {
                songsService.setList(currentPlayList);
            }
            isMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };


    /***********************Import songs***********************************/

    /**
     * This methods scan the folder and populate the songlist, and also update their info in database
     */
    public void getSongsList() {
        Field[] filesName = R.raw.class.getFields();

        for (int i = 0; i < filesName.length; i++) {
            int resourceId = getResources().getIdentifier(filesName[i].getName(), "raw", getPackageName());
            Uri musicUri = Uri.parse("android.resource://" + getPackageName() + "/" + Integer.toString(resourceId)  );

            try {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(getApplicationContext(), musicUri);
                String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

                if (artist == null) {
                    artist = "";
                }
                if (title == null) {
                    title = "";
                }
                if (album == null) {
                    album = "";
                }

                Song song = new Song(i, title, artist, album, songDao);
                song.uri = musicUri;
                listOfAllSongs.add(song);
                currentPlayList.add(song);
                if (songDao.isIntheDB(title, artist, album) == null) {
                    songDao.insertSong(song);
                }
            } catch (Exception e) {}
        }
    }


    public void getAlbumList() { // TODO test it after implemented song list.

        for ( Song song : listOfAllSongs) {
            if (!albumsMap.containsKey(song.getAlbum() + song.getArtist())) {
                Album album = new Album(song.getAlbum(), song.getArtist());
                album.getSongsInAlbum().add(song);
                albumsMap.put(song.getAlbum()+song.getArtist(), album);
            } else {
                Album album = albumsMap.get(song.getAlbum() + song.getArtist());
                album.getSongsInAlbum().add(song);
            }
        }

        albumsList = new ArrayList<Album>(albumsMap.values());
        java.util.Collections.sort(albumsList, new AlbumComparator());
    }



    class AlbumComparator implements Comparator<Album> {
        @Override
        public int compare(Album a, Album b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }

}
