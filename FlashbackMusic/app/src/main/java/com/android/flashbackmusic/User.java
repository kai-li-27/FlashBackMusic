package com.android.flashbackmusic;

import java.util.ArrayList;

/**
 * Created by kwmag on 3/7/2018.
 */

public interface User {

    void setRelationship(String relationship);

    void setAnonymousName(String anonymousName);

    void setListOfPlayedSongs(ArrayList<Song> listOfPlayedSongs);

    void setUserId(String userId);
}
