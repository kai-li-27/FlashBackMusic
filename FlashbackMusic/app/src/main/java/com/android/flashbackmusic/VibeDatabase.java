package com.android.flashbackmusic;
import android.location.Location;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by soyel on 3/3/2018.
 */

public class VibeDatabase {
    FirebaseDatabase database;
    DatabaseReference myRef;

    public VibeDatabase(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    public void insertSong(Song song) {
        //myRef.orderByChild("");
        DatabaseReference entry = myRef.push();
        myRef.child(song.getTitle()+ song.getUser()).setValue(song);

    }

    public void updateSong(Song song){
        DatabaseReference entry = myRef.child(song.getTitle()+ song.getUser());
        entry.setValue(song);
    }

    public ArrayList<Song> QueryByLocation(Location location, double radius){
        radius = radius;//ToDO convert radius into coordinates system
        Query query = myRef.orderByChild("lastLongitude").startAt(location.getLongitude() - radius)
                .endAt(location.getLongitude() + radius);
        query = query.getRef().orderByChild("lastLatitude").startAt(location.getLatitude() - radius)
                .endAt(location.getLatitude() + radius);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String, String> hashMap = (HashMap<String, String>)dataSnapshot.getValue();
                Song temp = new Song("","","",null);
                for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                    switch (entry.getKey()) {
                        case "album":
                            temp.setAlbum(entry.getValue());
                            break;
                        case "lastLongitude":
                            temp.setLastLongitude(Double.valueOf(entry.getValue()));
                    }
                }
                System.out.println(Double.toString( temp.getLastLongitude()));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
        return null ;
    }



}
