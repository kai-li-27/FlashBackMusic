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
import java.util.Date;
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

    private static VibeDatabase database;
    final ArrayList<String> downloadedAlbum = new ArrayList<>(); //This avoids downloading smae album over and voer again

    private ArrayList<VibeDatabaseEventListener> connectionChangedListeners = new ArrayList<>();





    /**
     * Do not call this!!!!!!!!!! use VibeDatabase.getDatabase();
     */
    private VibeDatabase() {
        myRef = FirebaseDatabase.getInstance().getReference();
        connectionStateRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectionStateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                connected = dataSnapshot.getValue(Boolean.class);
                for (VibeDatabaseEventListener i : connectionChangedListeners) {
                    i.onConnectionChanged(connected);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static VibeDatabase getDatabase() {
        if (database == null) {
            database = new VibeDatabase();
        }
        return database;
    }

    public void insertSong(Song song) {
        try {
            myRef.child(song.getDataBaseReferenceString()).setValue(song);
        } catch (Exception o) {o.printStackTrace();}
    }

    public void updateSong(Song song){
        try {
            DatabaseReference dataEntry = myRef.child(song.getDataBaseReferenceString());
            dataEntry.setValue(song);
        } catch(Exception e) {e.printStackTrace();}
    }


    /**
     * Query songs at location in range of radius. Each song in range is added to passed in songsList
     */
    public ArrayList<Song> queryByLocationOfAllSongs(final Location location, final double radiusInFeet, final ArrayList<Song> songsList){
        final double radiusInCordinate = radiusInFeet / 364605; //length of 1 latitude at 45 degrees

        Query query = myRef.orderByChild("lastLatitude").startAt(location.getLatitude() - radiusInCordinate)
                .endAt(location.getLatitude() + radiusInCordinate);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Song song = dataSnapshot.getValue(Song.class);
                if (song != null) {
                    if (song.getLastLocation().distanceTo(location) < radiusInFeet / 3.28) { //within radius

                        if (SongManager.getSongManager().isSongDownloaded(song) != null) {
                            Song downloaded = SongManager.getSongManager().isSongDownloaded(song);
                            song.setUri(downloaded.getUri());
                        } else {
                            DownloadSong.DownLoader downloader = new DownloadSong.DownLoader();
                            if (!downloadedAlbum.contains(song.getAlbum())) {
                                downloadedAlbum.add(song.getAlbum());
                                downloader.downloadSongForVibe(song);
                            }
                        }

                        System.out.println(song.getTitle() + " found in Firebase");

                        song.setUserDisplayName(AnonymousNameGenerator.GenerateAnonymousName(song.getEmail()));

                        //prevent testing fail
                        if (UserManager.getUserManager().getSelf() == null) {
                            songsList.add(song);
                            return;
                        }

                        if (song.getEmail().equals(UserManager.getUserManager().getSelf().getEmail())) {
                            song.setUserDisplayName("You");
                            song.setUserIdString(UserManager.getUserManager().getSelf().getUserId()); //This is for updating preference in vibe mode
                        } else {
                            if (UserManager.getUserManager().getFriends().containsKey(song.getEmail())) {
                                song.setUserDisplayName(UserManager.getUserManager().getFriends().get(song.getEmail()).getName());
                            }
                        }


                        song.updateDistance(location);
                        song.updateTimeDifference(new Date(System.currentTimeMillis()));
                        Algorithm.calculateSongWeightVibe(song);

                        if (songsList.contains(song)) {
                            int index = songsList.indexOf(song);
                            Song temp = songsList.get(index);
                            if (temp.getAlgorithmValue() < song.getAlgorithmValue()) {
                                songsList.remove(temp);
                            } else {
                                return;
                            }
                        }

                        int i = 0;
                        if (!songsList.isEmpty()) {
                            while (songsList.get(i).getUri() != null && song.getUri() == null) { // If the song is not downloaded, skip to the end of the list
                                i++;
                                if (i == songsList.size() - 1) {
                                    break;
                                }
                            }
                        }

                        for (; i < songsList.size(); i++) {

                            if (songsList.get(i).getAlgorithmValue() < song.getAlgorithmValue()) { //sorting by calculated weight
                                 songsList.add(i, song);
                                 return;
                            }
                        }
                       songsList.add(song);
                    }
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

    public ArrayList<Song> querySongsByUserId(String userId) {
        Query query = myRef.orderByChild("userIdString").equalTo(userId);

        final ArrayList<Song> songsList = new ArrayList<>();

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Song song = dataSnapshot.getValue(Song.class);
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


    public void upateInfoOfSongsOfUser(final ArrayList<Song> importedSongs) {
        Query query = myRef.orderByChild("userIdString").equalTo(UserManager.getUserManager().getSelf().getUserId());

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Song song = dataSnapshot.getValue(Song.class);
                if (song != null) {
                    Song temp = null;
                    for (Song i : importedSongs) { //Finds the song downloaded from Firebase in Importsonglist
                        if (i.getTitle().equals(song.getTitle()) && i.getArtist().equals(song.getArtist()) && i.getAlbum().equals(song.getAlbum())) {
                            temp = i;
                            break;
                        }
                    }

                    if (temp != null) {
                        temp.setLastTimeLong(song.getLastTimeLong());
                        temp.setLastLocation(song.getLastLocation());
                        temp.setPreference(song.getPreference());
                        SongManager.getSongManager().sortByDefault(); //This is toxic. Fix it if have time
                    }
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
    }
    public boolean isConnected() {
        return connected;
    }






//region Listener register and remove
    public void addConnectionChangedListener(VibeDatabaseEventListener listener) {
        connectionChangedListeners.add(listener);
    }

    public void removeConnectionChangedListener(VibeDatabaseEventListener listener) {
        connectionChangedListeners.remove(listener);
    }
//endregion;
}
