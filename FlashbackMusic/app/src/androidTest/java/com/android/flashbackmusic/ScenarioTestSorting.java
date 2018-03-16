package com.android.flashbackmusic;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.ViewAction;
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
import org.mortbay.jetty.Main;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import javax.net.ssl.ExtendedSSLSession;

import static org.junit.Assert.assertTrue;

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
            for (int i = 0; i < 4; i++) {
                current = new SongBuilder(Uri.EMPTY, names[i], emails[i])
                        .setArtist(artists[i])
                        .setAlbum(albums[i])
                        .setTitle(titles[i])
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

}
