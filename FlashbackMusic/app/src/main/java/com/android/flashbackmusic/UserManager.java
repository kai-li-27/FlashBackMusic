package com.android.flashbackmusic;


import java.util.ArrayList;

/**
 * Created by kwmag on 3/5/2018. Contains the list of all users as well as sets all the anonymous
 * names for each user, which must be handled by the UserManager since these users must have unique
 * pseudonyms
 */

public class UserManager {
    ArrayList<IUser> allUsers;
    ArrayList<IUser> friends;
    ArrayList<IUser> strangers;

    public ArrayList<IUser> getAllUsers() {
        return allUsers;
    }

    // set the list of users if you already have it
    public void setAllUsers(ArrayList<IUser> allUsers) {
        this.allUsers = allUsers;
    }

    // just add one individual user given the user
    public void addOneUserToList(IUser user) {
        allUsers.add(user);
    }

    public void addOneUserToList(String name, String email, String relationship, ArrayList<Song> songs, String userId) {
        IUser working;
        if (relationship.equalsIgnoreCase("stranger")) {
            working = new AnonymousUser("", "", email);
            strangers.add(working);
        } else if (relationship.equalsIgnoreCase("self")) {
            working = new SelfUser("You", userId, email);
        } else {
            working = new FriendUser(name, "", email);
            friends.add(working);
        }
        working.setListOfPlayedSongs(songs);
        allUsers.add(working);
    }

    // iterate through the users and set their anonymous names for the session
    public void createAnonymousNames() {
        for (IUser user: strangers) {
            // TODO algorithm to create anonymous names Iteration 4
            user.setName("FIXME");
        }
    }

}
