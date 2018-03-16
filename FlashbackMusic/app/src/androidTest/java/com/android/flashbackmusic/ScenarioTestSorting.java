package com.android.flashbackmusic;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.widget.Spinner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by kwmag on 3/15/2018.
 */

public class ScenarioTestSorting extends MainActivity implements UiThreadTest{
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


        // Looper.prepare();
/*
        new Thread() {
            public void run() {
                try {
                    Looper.prepare();*/
                    mainActivity.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Looper.prepare();
                            sortOptions.setSelection(1, true);
                            System.out.println("selected: " + sortOptions.getSelectedItem());
                            Looper.loop();
                        }
                    });/*
                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start(); */
        /*
        Looper.prepare();
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                sortOptions.setSelection(1, true);
            }
        });*/

        /*
        new Handler(Looper.getMainLooper().post(new Runnable() {
            @Override
            public void run() {

                sortOptions.setSelection(1, true);
            }
        })); */
        // sortOptions.setSelection(1); // sort by title pos == 1

        // SongManager.getSongManager().sortByTitle();
        for (int i = 0; i < songList.size() - 1; i++) {
            assertTrue(songList.get(i).getTitle().compareTo(songList.get(i+1).getTitle()) <= 0);
        }
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
