package com.android.flashbackmusic;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by kwmag on 3/10/2018.
 */

public class SortSongsOptionListener implements AdapterView.OnItemSelectedListener {
    SongManager songManager;
    private static final String TAG = "SortSongsOptionListener";

    public SortSongsOptionListener() {
        songManager = SongManager.getSongManager();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (pos == 0) {
            songManager.sortByDefault();
        } else if (pos == 1) {
            songManager.sortByTitle();
        } else if (pos == 2) {
            songManager.sortByAlbum();
        } else if (pos == 3) {
            songManager.sortByArtist();
        } else if (pos == 4) {
            songManager.sortByFavorites();
        } else {
            Log.e(TAG, "accessing invalid sort option");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
