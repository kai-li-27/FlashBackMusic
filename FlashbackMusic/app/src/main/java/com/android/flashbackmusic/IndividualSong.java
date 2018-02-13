package com.android.flashbackmusic;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class IndividualSong extends AppCompatActivity {

    private SongsService songsService;
    private MediaPlayer player;
    private Song currentSong;
    private Date date;
    private Intent playIntent;

    //private Button plus;
    //private static final int beautiful_pain = R.raw.beautiful_pain;
    //private static final int unstoppable = R.raw.unstoppable;


// handle the case when an individual songs are selected from many options, we will fetch that song
    // from many options in the database and play that song

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);

        if (playIntent == null) {
            playIntent = new Intent(this, SongsService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }




        // TOdo update these things in the database as well
        // long lasttime = sondDao.query(whatever is in the header);
        // songDao.update(currentSong);


        //TODO LoadMedia with the thing that was taken from the database, given a tag in main activity
        Button goBack = (Button) findViewById(R.id.button_back);

        goBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button plus = (Button) findViewById(R.id.button_favdisneu);
        plus.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        //TODO do something to change the look of the button
                        // todo do something when button is pressed when it has a certain look

                        currentSong = songsService.getCurrentSong();
                        currentSong.rotatePreference();
                        //TOdo update database

                    }
                }
        );


        Button reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.reset();
                    }

                });

        // play get stuff from the other activity, loads it from the database and then we playit
        // TOdo change UI of the play button to a pause button
        final Button play = (Button) findViewById(R.id.button_play);
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
                        //UI Change button and press button
                    }

                });


        Button skip = (Button) findViewById(R.id.button_skip);
        //TODO figure out what to do when it skips, probably iterate through
        // TOdo the song list
        skip.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        songsService.skip();
                        changeText();
                    }

                });


    }


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
        TextView loc = (TextView)findViewById(R.id.curr_song_location);
        loc.setText(currentSong.getLastLocation().toString());

        //curr_song_datetime
        TextView time = (TextView)findViewById(R.id.curr_song_datetime);
        time.setText(currentSong.getLastTime().toString());
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
            changeText();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
