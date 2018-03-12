package com.android.flashbackmusic;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by kwmag on 3/10/2018.
 */

public class SortSongsOptionListener implements AdapterView.OnItemSelectedListener {
    SongManager songManager;
    SongListAdapter adapter;
    private static final String TAG = "SortSongsOptionListener";

    public SortSongsOptionListener(SongListAdapter adapter) {
        songManager = SongManager.getSongManager();
        this.adapter = adapter;
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
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
