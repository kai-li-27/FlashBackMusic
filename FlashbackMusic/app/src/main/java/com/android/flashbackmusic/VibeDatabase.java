package com.android.flashbackmusic;
import android.location.Location;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by soyel on 3/3/2018.
 */

public class VibeDatabase {
    private DatabaseReference myRef;
    private DatabaseReference connectionStateRef;
    private boolean connected;

    private ArrayList<ConnectionChangedListener> connectionChangedListeners = new ArrayList<ConnectionChangedListener>();
    public void addConnectionChangedListener(ConnectionChangedListener listener) {
        connectionChangedListeners.add(listener);
    }


    public VibeDatabase(){
        myRef = FirebaseDatabase.getInstance().getReference();
        connectionStateRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectionStateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                connected = dataSnapshot.getValue(Boolean.class);
                for (ConnectionChangedListener i : connectionChangedListeners) {
                    i.onConnectionChanged(connected);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void insertSong(Song song) {
        myRef.child(song.getTitle()+ song.getUser()).setValue(song);
    }

    public void updateSong(Song song){
        DatabaseReference dataEntry = myRef.child(song.getTitle()+ song.getUser());
        dataEntry.setValue(song);
    }

    public ArrayList<Song> QueryByLocation(Location location, double radius){
        radius = radius;//ToDO convert radius into coordinates system

        Query query = myRef.orderByChild("lastLongitude").startAt(location.getLongitude() - radius)
                .endAt(location.getLongitude() + radius);
        query = query.getRef().orderByChild("lastLatitude").startAt(location.getLatitude() - radius)
                .endAt(location.getLatitude() + radius);

        final ArrayList<Song> songsList = new ArrayList<>();

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               Song song =dataSnapshot.getValue(Song.class);
               if (song != null) {
                   songsList.add(song);
               }
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
        return songsList;
    }

    public boolean getConnectionState() {
        return connected;
    }

}
