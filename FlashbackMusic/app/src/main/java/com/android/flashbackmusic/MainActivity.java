package com.android.flashbackmusic;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static final int MEDIA_RES_ID = R.raw.jazz_in_paris;
    private ArrayList<Song> songsList;
    private ListView songsView;

    public void loadMedia(int resourceId) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceId);
        try {
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mediaPlayer.isPlaying()) {
            ; //"do nothing"
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    public void getSongsList() {
        // TODO, FIXME
        songsList.add(new Song(1, "Jazz in Paris", "Human", "Jazzy"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songsView = (ListView) findViewById(R.id.song_list);
        songsList = new ArrayList<Song>();
        getSongsList();
        loadMedia(MEDIA_RES_ID); // load jazz in paris REMOVE LATER

        // could sort alphabetically for the songs


        SongListAdapter songAdapt = new SongListAdapter(this, songsList);
        songsView.setAdapter(songAdapt);

        // toggle between play and pause
        Button playButton = (Button) findViewById(R.id.button_play);
        playButton.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }

                    }
                });

        Button resetButton = (Button) findViewById(R.id.button_reset);
        resetButton.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mediaPlayer.reset();
                        loadMedia(MEDIA_RES_ID);
                    }
                });
    }
}
