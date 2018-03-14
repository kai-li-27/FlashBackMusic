package com.android.flashbackmusic;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by kwmag on 3/8/2018.
 */

public class JUnitTestsForPeople {

    UserManager manager;

    @Before
    public void initializeUserManager() {
        manager = UserManager.getUserManager();
    }

    @Test
    public void createFriendUserTest() {
        IUser person = new FriendUser("Kate", "10", "kww006@ucsd.edu");
        assertEquals(person.getName(), "Kate");
        assertEquals(person.getUserId(), "10");
        assertEquals(person.getEmail(), "kww006@ucsd.edu");
    }

    @Test
    public void createStrangerUserTest() {
        IUser person = new AnonymousUser("veryNotEddy", "20", "ecs003@ucsd.edu");
        assertEquals(person.getName(), "veryNotEddy");
        assertEquals(person.getUserId(), "20");
        assertEquals(person.getEmail(), "ecs003@ucsd.edu");
    }

    @Test
    public void createSelfUserTest() {
        IUser person = new SelfUser("You", "182405", "you@internet.com");
        assertEquals(person.getName(), "You");
        assertEquals(person.getUserId(), "182405");
        assertEquals(person.getEmail(), "you@internet.com");
    }


    @Test
    public void findAUserNotExisting() {
        IUser person = new FriendUser("Kate", "10", "kww006@ucsd.edu");
        IUser foundPerson = manager.findAUser("kww006@ucsd.edu");
        assertEquals(foundPerson, null); // should not be found
    }

    @Test
    public void findAUserExisting() {
        IUser person = new FriendUser("Kate", "10", "kww006@ucsd.edu");
        manager.addOneUserToList(person);
        IUser foundPerson = manager.findAUser("kww006@ucsd.edu");
        assertEquals(foundPerson, person); // should be found
    }



    @Test
    public void creatingAFriendTest() {
        manager.addOneUserToList("Roy", "rwfeng@ucsd.edu", "friend", null, "");
        assertEquals(manager.findAUser("rwfeng@ucsd.edu").getName(), "Roy");
    }

}
