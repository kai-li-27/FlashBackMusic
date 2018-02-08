package com.android.flashbackmusic;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IndividualSong extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Button plus;
    private static final int beautiful_pain = R.raw.beautiful_pain;
    private static final int unstoppable = R.raw.unstoppable;

// handle the case when an individual songs are selected from many options, we will fetch that song
    // from many options in the database and play that song
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);
        loadMedia(beautiful_pain);


        //TODO LoadMedia with the thing that was taken from the database, given a tag in main activity

        plus = (Button) findViewById(R.id.button_favdisneu);
        plus.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){

                    }
                }
        );


        Button reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        mediaPlayer.reset();
                        loadMedia(beautiful_pain);
                    }

                });

        // play get stuff from the other activity, loads it from the database and then we playit
        Button play = (Button) findViewById(R.id.button_play);
        play.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if(mediaPlayer.isPlaying()) mediaPlayer.pause();
                        else mediaPlayer.start();
                    }

                });


        Button skip = (Button) findViewById(R.id.button_skip);
        //TODO figure out what o do when it skips, probably iterate through
        // TOdo the song list
        skip.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        mediaPlayer.pause();
                        mediaPlayer.reset();
                        loadMedia(unstoppable);

                    }

                });


    }


    public void loadMedia(int resource_id){
        if(mediaPlayer == null) mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer MediaPlayer){mediaPlayer.start();}
        });

        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resource_id);
        try{
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        }catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
