package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by kwmag on 3/7/2018.
 */

public class SelfUser implements IUser {
    private String name;
    private String anonymousName;
    private ArrayList<Song> listOfPlayedSongs;
    private String userId;
    private String email;

    public SelfUser(String name, String userId, String email) {
        this.name = name;
        this.userId = userId;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAnonymousName(String anonymousName) {
        this.anonymousName = anonymousName;
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
        return name;
    }

    public String getAnonymousName() {
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
