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
 * converts the song list into a format ListView can use
 */

public class SongListAdapter extends BaseAdapter{

    private ArrayList<Song> songs;
    private LayoutInflater songInflater; // maps strings to textviews in song.xml
    private static final String TAG = "SongListAdapter";

    public SongListAdapter (Context c, ArrayList<Song> inSongs) {
        songs = inSongs;
        songInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
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
        Log.i(TAG, "getView; mapping list of songs to ListView");
        // mapping to the listview
        LinearLayout listLay = (LinearLayout) songInflater.inflate (R.layout.song, parent, false);
        TextView songView = (TextView) listLay.findViewById(R.id.song_title);
        TextView artistView = (TextView) listLay.findViewById(R.id.song_artist);

        Song currSong = songs.get(position);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());

        // set position as the tag for listview
        listLay.setTag(position);

        return listLay;
    }
}
