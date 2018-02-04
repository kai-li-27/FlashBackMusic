package com.android.flashbackmusic;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static final int MEDIA_RES_ID = R.raw.jazz_in_paris;

    public void loadMedia(int resourceId) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadMedia(MEDIA_RES_ID); // load jazz in paris

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
