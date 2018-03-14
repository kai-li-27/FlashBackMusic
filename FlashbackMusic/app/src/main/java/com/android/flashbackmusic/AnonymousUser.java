package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by kwmag on 3/7/2018. Note that name is not the actual name of the user, but the
 * pseudonym for the anonymous user
 */

public class AnonymousUser implements IUser {

    private String anonymousName;
    private ArrayList<Song> listOfPlayedSongs;
    private String userId;
    private String email;

    public AnonymousUser(String name, String userId, String email) {
        this.anonymousName = name;
        this.userId = userId;
        this.email = email;
    }

    public void setName(String anonymousName) {
        this.anonymousName = anonymousName;

    }

    public void addSongToSongList(Song song) {
        listOfPlayedSongs.add(song);
    }

    public void setListOfPlayedSongs(ArrayList<Song> listOfPlayedSongs) {
        this.listOfPlayedSongs = listOfPlayedSongs;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return anonymousName;
    }

    public ArrayList<Song> getListOfPlayedSongs() {
        return listOfPlayedSongs;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
