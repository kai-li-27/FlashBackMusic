package com.android.flashbackmusic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class IndividualSong extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_song);
        Bundle songInfo = getIntent().getExtras();
        String songName = songInfo.getString(Intent.EXTRA_TEXT);

    }
}
