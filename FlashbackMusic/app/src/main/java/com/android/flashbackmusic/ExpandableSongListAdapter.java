package com.android.flashbackmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kwmag on 3/10/2018.
 */

public class ExpandableSongListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Song> songList;
    private LayoutInflater listInflator;
    private List<String> listDataHeader;
    private HashMap<String, List<Song>> listDataChild;

    public ExpandableSongListAdapter(Context context, ArrayList<Song> songList, List<String> listDataHeader, HashMap <String,List<Song>> listChildData){
        this.context = context;
        this.listInflator = LayoutInflater.from(context);
        this.songList = songList;
        this.listDataChild = listChildData;
        this.listDataHeader = listDataHeader;
    }
    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.listDataChild.get(this.listDataHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.listDataHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.listDataChild.get(this.listDataChild.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String) getGroup(i);
        if (view == null) {
            LayoutInflater flater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = flater.inflate(R.layout.list_group, null);
        }

        TextView listHeader = (TextView) view.findViewById(R.id.previewSongsHeader);
        listHeader.setText(headerTitle);
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        view = (LinearLayout) listInflator.inflate (R.layout.song_indiv_preview, viewGroup, false);
        TextView songView = (TextView) view.findViewById(R.id.song_title);
        TextView artistView = (TextView) view.findViewById(R.id.song_artist);

        Song currSong = songList.get(i1);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());

        // set position as the tag for listview
        view.setTag(i1);

        return view;

    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
