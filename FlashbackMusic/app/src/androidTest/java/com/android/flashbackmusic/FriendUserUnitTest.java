package com.android.flashbackmusic;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by soyel on 3/14/2018.
 */

public class FriendUserUnitTest {
    String name = "name";
    String anonymousName = "anonymousName";
    ArrayList<Song> listOfPlayedSongs = new ArrayList<>();
    String userId = "userId";
    String email = "email";
    FriendUser friendUser;
    Song song = new Song();

    @Before
    public void initializeFriend(){
        friendUser = new FriendUser(name, userId, email);
    }
    @Test
    public void testUserName(){
        assert(friendUser.getName().equals(name));
    }

    @Test
    public void testUserId(){}

    @Test
    public void testUserEmail(){}
}
