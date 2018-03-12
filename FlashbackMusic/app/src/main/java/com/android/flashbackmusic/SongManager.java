package com.android.flashbackmusic;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by K on 3/8/2018.
 */

public class SongManager {
    private ArrayList<Song> listOfAllImportedSongs = new ArrayList<>();
    private ArrayList<Song> currentPlayList = new ArrayList<>();
    private ArrayList<Album> listOfAlbums = new ArrayList<>();
    private static final String TAG = "SongManager"; //for adding a new song\

    enum SortMode {
        TITLE, ALBUM, ARTIST, MOST_RECENT, PREFERENCE
    }

    private static SongManager instance;

    private SongManager() {

        Algorithm.importSongsFromResource(listOfAllImportedSongs);
        listOfAlbums = Algorithm.getAlbumList(listOfAllImportedSongs);
        currentPlayList = new ArrayList<>(listOfAllImportedSongs);

        if (UserManager.getUserManager().getSelf() == null) {
            Toast.makeText(App.getContext(), "You are not signed in, your play history won't be stored", Toast.LENGTH_LONG).show(); //This will make unit test fails. comment this out before unit test
        } else {
            String userId = UserManager.getUserManager().getSelf().getUserId();
            for (Song song : listOfAllImportedSongs) {
                song.setUserIdString(userId);
            }

            VibeDatabase.getDatabase().upateInfoOfSongsOfUser(listOfAllImportedSongs); //This will go to server and get the preference, location and time for each song
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


//region Sorting Methods
    public void sortByTitle(){
        for (int i = 0; i < listOfAllImportedSongs.size(); i++) { //go through whole list, finds largest each time
            Song temp = listOfAllImportedSongs.get(i);
            for (int j = i + 1; j < listOfAllImportedSongs.size(); j++) {
                if (temp.getTitle().compareTo(listOfAllImportedSongs.get(j).getTitle()) < 0) {
                    temp = listOfAllImportedSongs.get(j);
                }
            }
            listOfAllImportedSongs.remove(temp);
            listOfAllImportedSongs.add(0, temp);//insert the largest to the front
        }
    }


    void sortByAlbum() {
        listOfAllImportedSongs.clear(); //TODO make sure that songs in each album is sorted
        for (Album i : listOfAlbums) {
            listOfAllImportedSongs.addAll(i.getSongsInAlbum());
        }
    }


    void sortByArtist() {
        sortByTitle();
        for (int i = 0; i < listOfAllImportedSongs.size() - 1; i++) { // bubble sort
            for (int j = 0; j < listOfAllImportedSongs.size() - 1; j++) {
                if (listOfAllImportedSongs.get(j).getArtist().compareTo(listOfAllImportedSongs.get(j+1).getArtist()) > 0) {
                    Song temp = listOfAllImportedSongs.get(j);
                    listOfAllImportedSongs.remove(temp);
                    listOfAllImportedSongs.add(j+1, temp);
                }
            }
        }
    }


    void sortByDefault() {
        sortByTitle();
        for (int i = 0; i < listOfAllImportedSongs.size(); i++) { //go through whole list, finds least recent each time
            Song temp = listOfAllImportedSongs.get(i);
            for (int j = i + 1; j < listOfAllImportedSongs.size(); j++) {
                if (temp.getLastTime().compareTo(listOfAllImportedSongs.get(j).getLastTime()) >= 0) {
                    temp = listOfAllImportedSongs.get(j);
                }
            }
            listOfAllImportedSongs.remove(temp);
            listOfAllImportedSongs.add(0, temp);//insert the least recent to the front
        }
    }


    void sortByFavorites() {
        sortByTitle();
        for (int i = 0; i < listOfAllImportedSongs.size() - 1; i++) { //bubble sort
            for (int j = 0; j < listOfAllImportedSongs.size() - 1; j++) {
                if (listOfAllImportedSongs.get(j).getPreference() < listOfAllImportedSongs.get(j + 1).getPreference()) {
                    Song temp = listOfAllImportedSongs.get(j);
                    listOfAllImportedSongs.remove(temp);
                    listOfAllImportedSongs.add(j+1, temp);
                }
            }
        }
    }
//endregion;





//region Handle playlist change
    public void singleSongChosen() {
        currentPlayList.clear();
        currentPlayList.addAll(listOfAllImportedSongs);
    }

    public void albumChosen(int indexOfAlbum) {
        currentPlayList.clear();
        currentPlayList.addAll(listOfAlbums.get(indexOfAlbum).getSongsInAlbum());
    }

    public void vibeModeTurnedOn() {}

    public void vibeModeTurnedOff() {}
//endregion;
}
