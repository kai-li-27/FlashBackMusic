package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by K on 3/8/2018.
 */

public class SongManager {
    private ArrayList<Song> listOfAllImportedSongs = new ArrayList<>();
    private ArrayList<Song> currentPlayList = new ArrayList<>();
    private ArrayList<Album> listOfAlbums = new ArrayList<>();

    private static SongManager instance;

    private SongManager() {
        String userId = null;
        int i = 0;
        while(UserManager.getUserManager().getSelf() == null) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {};
            i++;
            if (i > 10) {
                break;
            }
        }

        if (i < 11) {
            userId =  UserManager.getUserManager().getSelf().getUserId();
        }

        Algorithm.importSongsFromResource(listOfAllImportedSongs);
        listOfAlbums = Algorithm.getAlbumList(listOfAllImportedSongs);
        currentPlayList = new ArrayList<>(listOfAllImportedSongs);

        if (userId != null) {
            for (Song song : listOfAllImportedSongs) {
                song.setUserIdString(userId);
            }

            VibeDatabase.getDatabase().upateInfoOfSongsOfUser(listOfAllImportedSongs);
        }
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

    }
    void sortByAlbum() {

    }
    void sortByArtist() {

    }
    void sortBy() {

    }

}
