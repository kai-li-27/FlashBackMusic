package com.android.flashbackmusic;

import android.net.Uri;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Spinner;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.action.ViewActions;
import org.hamcrest.Matchers;



import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;

/**
 * Created by kwmag on 3/15/2018.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScenarioTestSorting{
    ArrayList<Song> songList;


    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void initializeEverything() {
        songList = SongManager.getSongManager().getDisplaySongList();
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
            for (int i = 0; i < 4; i++) { // careful about Uri
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
    public void testSortByTitle() {
        assertTrue(songList.size() > 0);

        final Spinner sortOptions = mainActivity.getActivity().findViewById(R.id.sortingOptions);

        Espresso.onView(ViewMatchers.withId(R.id.sortingOptions)).perform(ViewActions.click());
        Espresso.onData(Matchers.allOf(Matchers.is(Matchers.instanceOf(String.class)), Matchers.is("Title"))).perform(ViewActions.click());


        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getTitle().compareTo(songList.get(i+1).getTitle()) <= 0);
        }
    }

    @Test
    public void testSortByAlbum() {
        assertTrue(songList.size() > 0);

        final Spinner sortOptions = mainActivity.getActivity().findViewById(R.id.sortingOptions);

        Espresso.onView(ViewMatchers.withId(R.id.sortingOptions)).perform(ViewActions.click());
        Espresso.onData(Matchers.allOf(Matchers.is(Matchers.instanceOf(String.class)), Matchers.is("Album"))).perform(ViewActions.click());


        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getAlbum().compareTo(songList.get(i+1).getAlbum()) <= 0);
        }
    }

    @Test
    public void testSortByArtist() {
        assertTrue(songList.size() > 0);

        final Spinner sortOptions = mainActivity.getActivity().findViewById(R.id.sortingOptions);

        Espresso.onView(ViewMatchers.withId(R.id.sortingOptions)).perform(ViewActions.click());
        Espresso.onData(Matchers.allOf(Matchers.is(Matchers.instanceOf(String.class)), Matchers.is("Artist"))).perform(ViewActions.click());


        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getArtist().compareTo(songList.get(i+1).getArtist()) <= 0);
        }
    }

    @Test
    public void testSortByTime() {
        assertTrue(songList.size() > 0);

        final Spinner sortOptions = mainActivity.getActivity().findViewById(R.id.sortingOptions);

        Espresso.onView(ViewMatchers.withId(R.id.sortingOptions)).perform(ViewActions.click());
        Espresso.onData(Matchers.allOf(Matchers.is(Matchers.instanceOf(String.class)), Matchers.is("Default"))).perform(ViewActions.click());


        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getLastTime().compareTo(songList.get(i+1).getLastTime())<= 0);
        }
    }
}
