package com.android.flashbackmusic;


import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kwmag on 3/5/2018. Contains the list of all users as well as sets all the anonymous
 * names for each user, which must be handled by the UserManager since these users must have unique
 * pseudonyms
 */

public class UserManager {
    private HashMap<String, IUser> allUsers;
    private HashMap<String, IUser> friends;
    private HashMap<String, IUser> strangers; // FIXME: change this to hashmap or something with key value pair

    private static final String TAG = "UserManager";

    public ArrayList<IUser> getUsersList(String relationship) {
        ArrayList<IUser> users = new ArrayList<>();
        HashMap<String, IUser> workingUsers;
        IUser working;

        if (relationship.equalsIgnoreCase("all")) {
            workingUsers = allUsers;
        } else if (relationship.equalsIgnoreCase("friends")) {
            workingUsers = friends;
        } else if (relationship.equalsIgnoreCase("strangers")) {
            workingUsers = strangers;
        } else {
            Log.e(TAG, "tried to get list of users that does not exist");
            return null;
        }

        for (String key: workingUsers.keySet()) {
            working = workingUsers.get(key);
            users.add(working);
        }
        return users;
    }

    public HashMap<String, IUser> getAllUsers() {
        return allUsers;
    }

    public HashMap<String, IUser> getFriends() {
        return friends;
    }

    public HashMap<String, IUser> getStrangers() {
        return strangers;
    }

    // set the list of users if you already have it
    public void setAllUsers(HashMap<String, IUser> allUsers) {
        this.allUsers = allUsers;
    }

    // just add one individual user given the user
    public void addOneUserToList(IUser user) {
        allUsers.put(user.getName(), user);
    }

    /*
     *
     */
    public void addOneUserToList(String name, String email, String relationship, ArrayList<Song> songs, String userId) {
        IUser working;
        if (relationship.equalsIgnoreCase("stranger")) {
            name = createOneAnonymousName();
            working = new AnonymousUser(name, "", email);
            strangers.put(name, working);
        } else if (relationship.equalsIgnoreCase("self")) {
            working = new SelfUser("You", userId, email);
        } else {
            working = new FriendUser(name, "", email);
            friends.put(name, working);
        }
        working.setListOfPlayedSongs(songs);
        allUsers.put(name, working);
    }

    public IUser findAUser(String name) {
        if (allUsers.get(name) == null) {
            Log.w(TAG, "User not in list of contacts");
        }
        return allUsers.get(name);
    }

    // iterate through the users and set their anonymous names for the session
    public void createAnonymousNames() {
        for (String key: strangers.keySet()) {
            // TODO algorithm to create anonymous names Iteration 4
            strangers.get(key).setName("FIXME");
        }
    }

    public String createOneAnonymousName() {
        // TODO algorithm to create anonymous names
        return "Bob";
    }

}
