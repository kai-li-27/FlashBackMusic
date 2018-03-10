package com.android.flashbackmusic;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by K on 3/8/2018.
 */

public class SongManager {
    private ArrayList<Song> listOfAllImportedSongs = new ArrayList<>();
    private ArrayList<Song> currentPlayList = new ArrayList<>();
    private ArrayList<Album> listOfAlbums = new ArrayList<>();
    private static final String TAG = "SongManager";

    private static SongManager instance;

    private SongManager() {
        Algorithm.importSongsFromResource(listOfAllImportedSongs);
        listOfAlbums = Algorithm.getAlbumList(listOfAllImportedSongs);
        currentPlayList = new ArrayList<>(listOfAllImportedSongs);
    }

    public static SongManager getSongManager() {
        if (instance == null) {
            instance = new SongManager();
        }
        return instance;
    }

    public ArrayList<Album> getAlbumList() {
        return listOfAlbums;
    }

    public ArrayList<Song> getCurrentPlayList() {
        return currentPlayList;
    }

    public ArrayList<Song> getDisplaySongList() {
        return listOfAllImportedSongs;
    }


    public void sortByTitle(){
        Log.i(TAG, "sortByTitle called");
    }
    void sortByAlbum() {
        Log.i(TAG, "sortByAlbum called");
    }
    void sortByArtist() {
        Log.i(TAG, "sortByArtist called");
    }
    void sortByDefault() {
        Log.i(TAG, "sortByDefault called");
    }
    void sortByFavorites() {
        Log.i(TAG, "sortbyFavorites called");
    }

}
