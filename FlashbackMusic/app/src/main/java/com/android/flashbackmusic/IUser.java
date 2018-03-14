package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by kwmag on 3/7/2018.
 */

public interface IUser {

    void setName(String name);

    void setListOfPlayedSongs(ArrayList<Song> listOfPlayedSongs);

    void addSongToSongList(Song song);

    void setUserId(String userId);

    void setEmail(String email);

    String getName();

    ArrayList<Song> getListOfPlayedSongs();

    String getUserId();

    String getEmail();
}
