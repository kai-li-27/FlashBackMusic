package com.android.flashbackmusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

/**
 * Created by ecsan on 2/7/2018.
 */

public class SongScanner {
    private SongsService musicPlayer;

    public SongScanner(SongsService musicPlayer){
        this.musicPlayer = musicPlayer;
    }
}
