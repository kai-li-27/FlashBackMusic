package com.android.flashbackmusic;
import android.location.Location;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

/**
 * Created by soyel on 3/3/2018.
 */

public class VibeDatabase {
    FirebaseDatabase database;
    DatabaseReference myRef;

    public VibeDatabase() {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    public void insertUser(long userID) {
        //myRef.orderByChild("");
        myRef.setValue(userID);
    }

    public void updateSong(){

    }

    public ArrayList<Song> QueryByLocation(Location location, double radius){
        return null;
    }



}
