package com.android.flashbackmusic;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class IndividualSong extends AppCompatActivity {

    private SongsService songsService = new SongsService();
    private MediaPlayer player = songsService.getMediaPlayer();
    private Song currentSong = songsService.getSong(songsService.getSongIndex());
    //private Button plus;
    //private static final int beautiful_pain = R.raw.beautiful_pain;
    //private static final int unstoppable = R.raw.unstoppable;


// handle the case when an individual songs are selected from many options, we will fetch that song
    // from many options in the database and play that song
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);
        //loadMedia(beautiful_pain);
        songsService.onCreate();
        changeText();

        //Todo update song's Lasttime to current time
        // Todo update song's lastLocation to current location
        //update these things in the database as well



        //TODO LoadMedia with the thing that was taken from the database, given a tag in main activity

        Button plus = (Button) findViewById(R.id.button_favdisneu);
        plus.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        //TODO do something to change the look of the button
                        // todo do something when button is pressed when it has a certain look


                        int songIndex = songsService.getSongIndex();
                        currentSong = songsService.getSong(songIndex);
                        currentSong.rotateProference();
                        //TOdo update database

                    }
                }
        );


        Button reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        player.reset();

                        //loadMedia(beautiful_pain);
                    }

                });

        // play get stuff from the other activity, loads it from the database and then we playit
        // TOdo change UI of the play button to a pause button?????????
        Button play = (Button) findViewById(R.id.button_play);
        play.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if(player.isPlaying()) player.pause();
                        else player.start();
                    }

                });


        Button skip = (Button) findViewById(R.id.button_skip);
        //TODO figure out what to do when it skips, probably iterate through
        // TOdo the song list
        skip.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        player.pause();
                        songsService.setSong(songsService.getSongIndex() + 1);
                        player.reset();
                        changeText();
                        songsService.playSong();
                        //loadMedia(unstoppable);

                    }

                });


    }


    public void loadMedia(int resource_id){
        if(player == null) player = new MediaPlayer();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer MediaPlayer){player.start();}
        });

        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resource_id);
        try{
            player.setDataSource(assetFileDescriptor);
            player.prepareAsync();
        }catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void changeText(){
        currentSong = songsService.getSong(songsService.getSongIndex());

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
}
