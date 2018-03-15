package com.android.flashbackmusic;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by kai on 3/5/2018.
 */
@RunWith(AndroidJUnit4.class)
public class PlaylistAlgorithmUnitTests {
    ArrayList<Song> songList;
    SongBuilder songBuilder1,songBuilder2,songBuilder3;
    Song testSong1, testSong2, testSong3;
    String username = "eddy";
    String email = "ecs003@ucsd.edu";
    Uri uri = new Uri.Builder().build();


    @Before
    public void initilaizeSongs() {

        // should be around 2
        songBuilder1 = new SongBuilder(uri,username,email);
        testSong1 = songBuilder1.build();
        testSong1.setDistance(100);
        testSong1.setTimeDifference(100);
        testSong1.setEmail("ecs003@ucsd.edu");


        // not in week and not friend so it should be under 1
        songBuilder2 = new SongBuilder(uri,username,email);
        testSong2 = songBuilder2.build();
        testSong2.setDistance(10000);
        testSong2.setTimeDifference(604800005);
        testSong2.setEmail("notfriend@gmail.ucsd");

        Log.i("myTag","Time Difference: " + testSong2.getTimeDifference());

        // check if distance goes negative and value should be low
        songBuilder3 = new SongBuilder(uri,username,email);
        testSong3 = songBuilder3.build();
        testSong3.setDistance(1000000000);
        testSong3.setTimeDifference(999999999);
        testSong3.setEmail("kww006@ucsd.edu");

    }

    @Test
    public void checktestSong1() {
        double result;
        result = Algorithm.calculateSongWeightVibe(testSong1);

        assertEquals(2.0,result,.1);
        Log.i("myTag1","result 1: " + result);
    }

    @Test
    public void checktestSong2() {
        double result;
        result = Algorithm.calculateSongWeightVibe(testSong2);
        Log.i("myTag2","result 2: " + result);
        assertEquals(1.9,result,.1);
    }

    @Test
    public void checktestSong3() {
        double result;
        result = Algorithm.calculateSongWeightVibe(testSong3);

        assertEquals(1.9,result,.1);
        Log.i("myTag3","result 3: " + result);
    }




}
