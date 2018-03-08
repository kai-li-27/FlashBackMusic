package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by kwmag on 3/7/2018.
 */

public class AnonymousUser implements IUser {

    private String relationship;
    private String anonymousName;
    private ArrayList<Song> listOfPlayedSongs;
    private String userId;

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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
}
