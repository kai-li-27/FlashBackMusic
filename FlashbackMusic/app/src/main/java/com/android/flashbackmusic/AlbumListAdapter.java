package com.android.flashbackmusic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * converts a list of albums into a format ListView can display
 */

public class AlbumListAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private LayoutInflater albumInflater; // maps strings to textviews in song.xml
    private static final String TAG = "AlbumListAdapter";

    public AlbumListAdapter (Context c, ArrayList<Album> inAlbums) {
        albums = inAlbums;
        albumInflater = LayoutInflater.from(c);
    }

    /**
     * Gets the size of the list of albums
     */
    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Gets the listview
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // mapping to the listview
        Log.i(TAG, "getView; mapping album list to the ListView");
        LinearLayout listLay = (LinearLayout) albumInflater.inflate (R.layout.album, parent, false);
        TextView albumView = (TextView) listLay.findViewById(R.id.album_title);
        TextView artistView = (TextView) listLay.findViewById(R.id.album_artist);

        Album currAlbum = albums.get(position);
        albumView.setText(currAlbum.getName());
        artistView.setText(currAlbum.getArtist());

        // set position as the tag for listview
        listLay.setTag(position);

        return listLay;
    }
}
