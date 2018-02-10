package com.android.flashbackmusic;


import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;

public class IndividualSong extends AppCompatActivity {

    private SongsService songsService = new SongsService();
    private MediaPlayer player = songsService.getMediaPlayer();
    private Song currentSong = songsService.getSong(songsService.getSongIndex());
    private Uri uri;
    private Date date;
    //Todo location variable
    private SongDao songDao;
    private SongDatabase Db;
    private LocationManager locationManager;





    private final LocationListener mLocationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {

        }
    };
    //private Button plus;
    //private static final int beautiful_pain = R.raw.beautiful_pain;
    //private static final int unstoppable = R.raw.unstoppable;


// handle the case when an individual songs are selected from many options, we will fetch that song
    // from many options in the database and play that song

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);
        Bundle songInfo = getIntent().getExtras();
        String songName = songInfo.getString(Intent.EXTRA_TEXT);

        Button goBack = (Button) findViewById(R.id.button_back);

        goBack.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //loadMedia(beautiful_pain);
        songsService.onCreate();
        changeText();

        //update song's Lasttime to current time
        date = Calendar.getInstance().getTime();
        currentSong.setLastTime(date);

        // Todo update song's lastLocation to current location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, );
        currentSong.setLastLocation();




        // TOdo update these things in the database as well
        // long lasttime = sondDao.query(whatever is in the header);
        // songDao.update(currentSong);


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
        // TOdo change UI of the play button to a pause button
        Button play = (Button) findViewById(R.id.button_play);
        play.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){

                        if(player.isPlaying()) player.pause();
                        else player.start();
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
