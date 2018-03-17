package com.android.flashbackmusic;

import android.net.Uri;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static junit.framework.Assert.assertTrue;

/**
 * Created by kwmag on 3/16/2018.
 */

public class ScenarioTestVibeMode {
    ArrayList<Song> songList;
    UserManager userManager;

    @Rule
    public ActivityTestRule<IndividualSong> individualSong = new ActivityTestRule<IndividualSong>(IndividualSong.class);

    @Before
    public void initializeEverything() {
        songList = SongManager.getSongManager().getVibeSongList();
        userManager = UserManager.getUserManager();
        if (songList.size() == 0) {
            System.out.println("No songs in the list, adding fake songs");
            Song current;
            String[] names = {"Bob", "Cat", "Dog", "Apple"};
            String[] emails = {"bob@fake.com", "cat@fake.com", "dog@fake.com", "apple@fake.com"};
            String[] artists = {"Alpha man", "Beta woman", "Chi someone", "Donkey man"};
            String[] titles = {"Billy Jeans", "Caravan", "Dinosaur Blues", "Alpaca Llama"};
            String[] albums = {"Clues", "Dare", "Altruism", "Berries"};
            String[] relationships = {"friend", "stranger", "self", "stranger"};
            int[] favorites = {2,0,1,2};
            long[] times = {1,2,3,4};
            for (int i = 0; i < 4; i++) {
                current = new SongBuilder(Uri.EMPTY, names[i], emails[i])
                        .setArtist(artists[i])
                        .setAlbum(albums[i])
                        .setTitle(titles[i])
                        .setLastTimeLong(times[i])
                        .build();
                ArrayList<Song> currSongs = new ArrayList<>();
                currSongs.add(current);
                userManager.addOneUserToList(names[i], emails[i], relationships[i], currSongs, "" + i);
                songList.add(current);
            }
        } else {
            System.out.println("songList is not empty");
        }

        Espresso.onView(ViewMatchers.withId(R.id.flashback_switch)).perform(ViewActions.click());

    }

    // test for dropdown button
    @Test
    public void testSeeWhatPlaysNext() {
        assertTrue(songList.size() > 0);
        ExpandableListView expListView = individualSong.getActivity().findViewById(R.id.previewNextSongsList);
        Espresso.onView(ViewMatchers.withId(R.id.previewNextSongsList)).check(matches(isDisplayed()));
    } // already have unit test to check contents are correctly sorted

    // Test for labels(location, time, who played it) displaying and updating.
    @Test
    public void testSeeLabels() {
        TextView whoPlayedIt = individualSong.getActivity().findViewById(R.id.curr_song_user);
        TextView wherePlayedIt = individualSong.getActivity().findViewById(R.id.curr_song_location);
        TextView timePlayedIt = individualSong.getActivity().findViewById(R.id.curr_song_datetime);
        assertTrue(!(whoPlayedIt.getText().toString().equals("Uninitialized User")));
        // assertTrue(whoPlayedIt.getText().toString().equals("Bob"));
        assertTrue(!(wherePlayedIt.getText().toString().equals("Uninitialized Song Location")));
        assertTrue(!(timePlayedIt.getText().toString().equals("Uninitialized Song Date and Time")));
    }

    // Test for enter Vibe Mode button.
    @Test
    public void testGetOutOfVibeMode() {
        Espresso.onView(ViewMatchers.withId(R.id.flashback_switch)).perform(ViewActions.click());
    }
}

