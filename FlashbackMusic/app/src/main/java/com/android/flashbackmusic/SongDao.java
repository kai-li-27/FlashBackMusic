package com.android.flashbackmusic;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

/**
 * Created by ecsan on 2/7/2018.
 */

@Dao
public interface SongDao {

    @Query ("Select lastLongitude FROM song where title = :title AND album = :album AND artist = :artist")
    public double queryLastLongitude(String title, String artist, String album);

    @Query ("Select lastAltitude FROM song where title = :title AND album = :album AND artist = :artist")
    public double queryLastAltitude(String title, String artist, String album);

    @Query("Select lastTimeLong FROM song where title = :title AND album = :album AND artist = :artist")
    public long queryLastTime(String title, String artist, String album);

    @Query("SELECT preference FROM song where title = :title AND album = :album AND artist = :artist")
    public int queryPreference(String title, String artist, String album);

    @Query ("Select * FROM song where title = :title AND album = :album AND artist = :artist")
    public Song isIntheDB(String title, String artist, String album);


    @Insert
    public void insertSong(Song song);

    @Update
    public void updateSong(Song... song);

    @Insert
    public void insertAllSong(Song... songs);

    @Delete
    public void deleteSong(Song song);


}
