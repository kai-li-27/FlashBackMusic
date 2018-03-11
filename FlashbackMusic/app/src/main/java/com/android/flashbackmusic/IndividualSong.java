package com.android.flashbackmusic;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * screen that displays when a song is playing
 */
public class IndividualSong extends AppCompatActivity implements SongServiceEventListener, VibeDatabaseEventListener {

    private SongService songsService;
    private final String[] DAYSINWEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final String[] TIMERANGE = {"Morning", "Afternoon", "Night"};
    private static final String TAG = "IndividualSong";

    /*
    ExpandableSongListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Song>> listDataChild;
    */

    ArrayList<Song> upcomingList = new ArrayList<>();


//region Handlers of IndividualActivity
    /**
     * Override back button behavior to not allow user to go back to mainActivity while in flashback mode
     */
    @Override
    public void onBackPressed() {
        Log.v(TAG, "back button pressed");
        if (!songsService.getFlashBackMode()) {
            finish();
        } else {
            Toast.makeText(this, "If you want to choose songs to play, exit Flashback mode first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected  void onDestroy() {
        unbindService(musicConnection);
        super.onDestroy(); //TODO remove listener
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);


        // Binds to the music service
        Intent intent = new Intent(this, SongService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);


        ArrayList listDataHeader = new ArrayList<String>();
        listDataHeader.add("Upcoming Songs");
        HashMap listDataChild = new HashMap<String,List<Song>>();
        listDataChild.put("Upcoming Songs", upcomingList);
        ExpandableListView expListView = findViewById(R.id.previewNextSongsList);
        ExpandableListAdapter listAdapter = new ExpandableSongListAdapter(this, upcomingList, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        Button goBack = findViewById(R.id.button_back);
        goBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!songsService.getFlashBackMode()) {
                    finish(); //TODO remove listener
                } else {
                    Toast.makeText(IndividualSong.this, "If you want to choose songs to play, exit Flashback mode first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button plus = findViewById(R.id.button_favdisneu);
        plus.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Song currentSong = songsService.getCurrentSong(); //TODO this is bad. Refactor to songsservice if have time
                        currentSong.rotatePreference();
                        VibeDatabase.getDatabase().updateSong(currentSong);
                        //changes look of button
                        changeDisplay(currentSong);
                    }
                });

        Button reset = findViewById(R.id.button_reset);
        reset.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.reset();
                    }
                });

        Button play = findViewById(R.id.button_play);
        play.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.playPause();
                    }
                });

        Button skip = findViewById(R.id.button_skip);
        skip.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.playNext();
                    }
                });

        final Switch mySwitch = findViewById(R.id.flashback_switch);
        mySwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        songsService.switchMode(checked);

                        if (!songsService.getFlashBackMode() ) {
                            mySwitch.setChecked(false);
                        }
                    }
                });
    }
//endregion;





//region UI change methods

    /**
     * preparing the list data
     * */
    private void prepareListData(){
        Song currentSong = songsService.getCurrentSong();
        ArrayList<Song> currentPlayList = SongManager.getSongManager().getCurrentPlayList();
        int currentIndex = currentPlayList.indexOf(currentSong);

        upcomingList.clear();
        for (int i = currentIndex + 1; i < currentPlayList.size(); i++) {
            upcomingList.add(currentPlayList.get(i));
        }
    }


    /**
     * Change the icon of +/-/check button
     */
    private void changeDisplay(Song currentSong){
        Log.v(TAG, "toggling favorite/dislike button");
        Button plus = findViewById(R.id.button_favdisneu);
        int[] appearance = new int [3];
        appearance[0] = R.drawable.flashback_plus_inactive;
        appearance[1] = R.drawable.flashback_checkmark_inactive;
        appearance[2] = R.drawable.flashback_minus_inactive;
        plus.setBackgroundResource(appearance[currentSong.getPreference()]);
    }

    /**
     * Change the icon of play/pause button
     */
    public void playPause(boolean isPlaying){
        Log.v(TAG, "play/pause button pressed");
        Button play = findViewById(R.id.button_play);
        int playButton = (!isPlaying)? R.drawable.flashback_play_inactive : R.drawable.flashback_pause_inactive;
        play.setBackgroundResource(playButton);
     }


    /**
     * Change the text of last time, last location, song title, artist and album
     */
    @SuppressLint("StaticFieldLeak")
    public void changeText(final Song song){
        Log.v(TAG, "changing text info of current song");

        //curr_song_title
        TextView title = findViewById(R.id.curr_song_title);
        title.setText(song.getTitle());

        //curr_song_artist
        TextView artist = findViewById(R.id.curr_song_artist);
        artist.setText("by " + song.getArtist());

        //curr_song_album
        TextView album = findViewById(R.id.curr_song_album);
        album.setText("Album: " + song.getAlbum());

        //curr_song_user
        TextView user = findViewById(R.id.curr_song_album);
        //user.setText("User: " + song.getDisplayUser); TODO: need method to retrieve correct user name and display it either as a Anonymous title, Friend, or You

        //get the name of the location, running on another thread
        new AsyncTask<Void, Void, Void>() {
            String addressName;

            @Override
            protected Void doInBackground(Void... params) {
                Geocoder geocoder = new Geocoder(IndividualSong.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(song.getLastLocation().getLatitude(), song.getLastLocation().getLongitude(), 1);
                    if (addressList != null && addressList.size() > 0) {
                        // Help here to get only the street name
                        Address address = addressList.get(0);
                        addressName = address.getAddressLine(0);
                        if (addressName == null) { // In case can't get specific address
                            addressName = address.getThoroughfare();
                        }
                    } else {
                        addressName = "Location Unavailable";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    addressName =  "Location Unavailable";
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                TextView loc = findViewById(R.id.curr_song_location);
                loc.setText(addressName);
            }
        }.execute();

        //curr_song_datetime
        TextView time = findViewById(R.id.curr_song_datetime);
        if (song.getLastTime() == null) {
            time.setText("Time Unavailable");
        } else {
            time.setText(DAYSINWEEK[song.getLastTime().getDay()] + " "
                    + TIMERANGE[song.timeRange(song.getLastTime().getHours())]
                    + ", " + DateFormat.getTimeInstance(DateFormat.SHORT).format(song.getLastTime()));
        }
    }
//endregion;


    /**
     * Establish connection to songsService, and play the chosen song
     */
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.MusicBinder binder = (SongService.MusicBinder)service;
            songsService = binder.getService();
            songsService.addSongServiceEventListener(IndividualSong.this);
            Bundle bundle = getIntent().getExtras();
            int index = bundle.getInt(Intent.EXTRA_INDEX);
            songsService.loadMedia(index);
            songsService.playPause(); //start playing TODO this is bad, create startplaying()

            Switch mySwitch = findViewById(R.id.flashback_switch);
            if (songsService.getFlashBackMode()) {
                mySwitch.setChecked(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };





//region SongServiceEventListener Handlers
    @Override
    public void onSongLoaded(Song loadedSong) {
        changeText(loadedSong);
        changeDisplay(loadedSong);
        prepareListData();
    }

    @Override
    public void onSongCompleted(Song completedSong, Song nextSong) {

    }

    @Override
    public void onSongPaused(Song currentSong) {
        playPause(false);
    }

    @Override
    public void onSongSkipped(Song skippedSong, Song nextSong) {

    }

    @Override
    public void onSongResumed(Song currentSong) {
        playPause(true);
    }

    @Override
    public void onVibeModeToggled(boolean vibeModeOn) {
        final ConstraintLayout indivSongActivity = findViewById(R.id.individualsongactivity);
        if (vibeModeOn) {
            indivSongActivity.setBackgroundColor(Color.parseColor("#D6CEF2"));
        } else {
            indivSongActivity.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }
//endregion;





//region VibeDatabaseEventListener Handlers
    @Override
    public void onConnectionChanged(boolean connected) {

    }
//endregion;
}
