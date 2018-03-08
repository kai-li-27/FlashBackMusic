package com.android.flashbackmusic;


import java.util.ArrayList;

/**
 * Created by kwmag on 3/5/2018. Contains the list of all users as well as sets all the anonymous
 * names for each user, which must be handled by the UserManager since these users must have unique
 * pseudonyms
 */

public class UserManager {
    ArrayList<IUser> allUsers;

    public ArrayList<IUser> getAllUsers() {
        return allUsers;
    }

    // set the list of users
    public void setAllUsers(ArrayList<IUser> allUsers) {
        this.allUsers = allUsers;
    }

    public void addOneUserToList(IUser user) {
        allUsers.add(user);
    }

    // iterate through the users and set their anonymous names for the session
    public void createAnonymousNames() {
        for (IUser user: allUsers) {
            // TODO algorithm to create anonymous names Iteration 4
            user.setAnonymousName("FIXME");
        }
    }

}
