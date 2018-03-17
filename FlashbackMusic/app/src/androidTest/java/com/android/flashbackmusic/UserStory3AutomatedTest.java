package com.android.flashbackmusic;
import android.net.Uri;

import android.support.test.rule.ActivityTestRule;
import android.widget.Button;



import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.util.ArrayList;


import static junit.framework.Assert.assertTrue;

/**
 * Created by K on 3/16/2018.
 */

public class UserStory3AutomatedTest {

    ArrayList<Song> songList;
    SongService songsService;
    @Rule
    public ActivityTestRule<IndividualSong> individualActivity = new ActivityTestRule<IndividualSong>(IndividualSong.class);

    @Before
    public void initializeEverything() {

        songList = SongManager.getSongManager().getCurrentPlayList();
        if (songList.size() == 0) {
            System.out.println("No songs in the list, adding fake songs");
            Song current;
            String[] names = {"Bob", "Cat", "Dog", "Apple"};
            String[] emails = {"bob@fake.com", "cat@fake.com", "dog@fake.com", "apple@fake.com"};
            String[] artists = {"Alpha man", "Beta woman", "Chi someone", "Donkey man"};
            String[] titles = {"Billy Jeans", "Caravan", "Dinosaur Blues", "Alpaca Llama"};
            String[] albums = {"Clues", "Dare", "Altruism", "Berries"};
            int[] favorites = {2,0,1,2};
            long[] times = {1,2,3,4};
            for (int i = 0; i < 4; i++) {
                current = new SongBuilder(Uri.EMPTY, names[i], emails[i])
                        .setArtist(artists[i])
                        .setAlbum(albums[i])
                        .setTitle(titles[i])
                        .setLastTimeLong(times[i])
                        .build();
                songList.add(current);
            }
        } else {
            System.out.println("songList is not empty");
        }
    }

    @Test
    public void testDisplayUpcomingTrack() {
        assertTrue(songList.size() > 0);
        IndividualSong activity = individualActivity.getActivity();
        ArrayList<Song> upcomingList = activity.getUpcomingList();
        songsService = activity.getSongsService();

        assertTrue(upcomingList.get(0).equals(songList.get(1)));
        assertTrue(upcomingList.get(1).equals(songList.get(2)));

        Song songToPlay = upcomingList.get(0);
        Button skip = activity.findViewById(R.id.button_skip);
        skip.callOnClick();

        assertTrue(songToPlay.equals(songsService.getCurrentSong()));
        assertTrue(upcomingList.get(0).equals(songList.get(2)));
        assertTrue(upcomingList.get(1).equals(songList.get(3)));
    }

}
