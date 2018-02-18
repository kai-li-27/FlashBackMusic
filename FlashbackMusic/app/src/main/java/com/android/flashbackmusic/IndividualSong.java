package com.android.flashbackmusic;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

public class IndividualSong extends AppCompatActivity {

    private SongsService songsService;
    private MediaPlayer player;
    private Song currentSong;
    private final String[] DAYSINWEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private final String[] TIMERANGE = {"Morning", "Noon", "Afternoon"};

    @Override
    protected  void onDestroy() {
        unbindService(musicConnection);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);

        // Binds to the music service
        Intent intent = new Intent(this, SongsService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);

        Button goBack = (Button) findViewById(R.id.button_back);
        goBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final Button plus = (Button) findViewById(R.id.button_favdisneu);
        plus.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        currentSong = songsService.getCurrentSong();
                        currentSong.rotatePreference();
                        //changes look of button
                        changeDisplay(plus);
                    }
                });

        Button reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.reset();
                    }
                });

        Button play = (Button) findViewById(R.id.button_play);
        play.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if (player.isPlaying()) {
                            player.pause();
                        }
                        else {
                            player.start();
                        }
                        playPause();
                    }
                });

        Button skip = (Button) findViewById(R.id.button_skip);
        skip.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.skip();
                        changeText();
                        changeDisplay(plus);
                    }
                });

        Switch mySwitch = (Switch) findViewById(R.id.flashback_switch);
        final ConstraintLayout indivSongActivity = (ConstraintLayout) findViewById(R.id.individualsongactivity);
        mySwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked && !songsService.getFlashBackMode()) {
                            songsService.switchMode();
                            indivSongActivity.setBackgroundColor(Color.parseColor("#f2d5b8"));
                        } else if (checked && songsService.getFlashBackMode()) {
                            indivSongActivity.setBackgroundColor(Color.parseColor("#f2d5b8"));
                        } else if (!checked && songsService.getFlashBackMode()) {
                            songsService.switchMode();
                            indivSongActivity.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        } else {
                            indivSongActivity.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                });
    }

    private void changeDisplay(Button button){
        currentSong = songsService.getCurrentSong();
        int[] appearance = new int [3];
        appearance[0] = R.drawable.flashback_plus_inactive;
        appearance[1] = R.drawable.flashback_checkmark_inactive;
        appearance[2] = R.drawable.flashback_minus_inactive;
        button.setBackgroundResource(appearance[currentSong.getPreference()]);
    }

    public void playPause(){
        Button play = (Button) findViewById(R.id.button_play);
        int playButton = (!player.isPlaying())? R.drawable.flashback_play_inactive : R.drawable.flashback_pause_inactive;
        play.setBackgroundResource(playButton);
     }


    @SuppressLint("StaticFieldLeak")
    public void changeText(){
        currentSong = songsService.getCurrentSong();

        //curr_song_title
        TextView title = (TextView)findViewById(R.id.curr_song_title);
        title.setText(currentSong.getTitle());

        //curr_song_artist
        TextView artist = (TextView)findViewById(R.id.curr_song_artist);
        artist.setText(currentSong.getArtist());

        //curr_song_album
        TextView album = (TextView)findViewById(R.id.curr_song_album);
        album.setText(currentSong.getAlbum());

        //curr_song_location
        //TextView loc = (TextView)findViewById(R.id.curr_song_location);
        //loc.setText(getAddressFromLocation(currentSong.getLastLocation()));
        new AsyncTask<Void, Void, Void>() {
            String addressName;

            @Override
            protected Void doInBackground(Void... params) {
                Geocoder geocoder = new Geocoder(IndividualSong.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(currentSong.getLastLocation().getLatitude(), currentSong.getLastLocation().getLongitude(), 1);
                    if (addressList != null && addressList.size() > 0) {
                        // Help here to get only the street name
                        Address address = addressList.get(0);
                        addressName = address.getLocality();
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
                TextView loc = (TextView)findViewById(R.id.curr_song_location);
                loc.setText(addressName);
            }
        }.execute();

        //curr_song_datetime
        TextView time = (TextView)findViewById(R.id.curr_song_datetime);
        if (currentSong.getLastTime() == null) {
            time.setText("Time Unavailable");
        } else {
            time.setText(DAYSINWEEK[currentSong.getLastTime().getDay()] + " "
                    + TIMERANGE[currentSong.timeRange(currentSong.getLastTime().getHours())]
                    + ", " + DateFormat.getTimeInstance(DateFormat.SHORT).format(currentSong.getLastTime()));
        }
    }


    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongsService.MusicBinder binder = (SongsService.MusicBinder)service;
            songsService = binder.getService();
            player = songsService.getMediaPlayer();
            Bundle bundle = getIntent().getExtras();
            int index = bundle.getInt(Intent.EXTRA_INDEX);
            songsService.loadMedia(index);
            songsService.setCurrentIndividualSong(IndividualSong.this);
            // Change the look according to current song
            changeText();
            changeDisplay((Button)findViewById(R.id.button_favdisneu));

            Switch mySwitch = (Switch) findViewById(R.id.flashback_switch);
            if (songsService.getFlashBackMode()) {
                mySwitch.setChecked(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
