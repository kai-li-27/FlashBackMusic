package com.android.flashbackmusic;
import android.net.Uri;

import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.logging.AtraceLogger;
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
        Algorithm.importSongsFromResource(songList, 4);
    }

    @Test
    public void testDisplayUpcomingTrack() {
        assertTrue(songList.size() > 0);
        IndividualSong activity = individualActivity.getActivity();
        ArrayList<Song> upcomingList = activity.getUpcomingList();
        songsService = activity.getSongsService();

        Button skip = activity.findViewById(R.id.button_skip);
        skip.callOnClick();

        assertTrue(upcomingList.get(0).equals(songList.get(2)));
        assertTrue(upcomingList.get(1).equals(songList.get(3)));

        Song songToPlay = upcomingList.get(0);
        skip.callOnClick();


        assertTrue(songToPlay.equals(songsService.getCurrentSong()));
        assertTrue(upcomingList.get(0).equals(songList.get(3)));
        assertTrue(upcomingList.get(1).equals(songList.get(4)));
    }

}
