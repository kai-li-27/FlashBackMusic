package com.android.flashbackmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kate and Camron on 2/7/2018.
 */

public class AlbumListAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private LayoutInflater albumInflater; // maps strings to textviews in song.xml

    public AlbumListAdapter (Context c, ArrayList<Album> inAlbums) {
        albums = inAlbums;
        albumInflater = LayoutInflater.from(c);
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // mapping to the listview
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