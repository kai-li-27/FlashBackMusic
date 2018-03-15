package com.android.flashbackmusic;

import android.app.Application;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by K on 2/20/2018.
 */


public final class Algorithm {

    /**
     * @precondition songList is not null, and is empty
     * @postcondition songList will be filled with the songs under Resource.raw folder.
     * @postcondition songs in songList will be sorted by alphabetical order of song title
     * @postcondition each object in songList will be set the following fields: title, artist, album, musicUri
     * @param songsList
     */
    public static void importSongsFromResource(ArrayList<Song> songsList) {
        if (songsList == null) {
            System.err.println("Argument passed into importSongsFromResource() is null.");
            throw new IllegalArgumentException();
        }
        if (songsList.size() != 0) {
            System.err.println("Argument passed into importSongsFromResource() is not empty.");
            throw new IllegalArgumentException();
        }
        Field[] filesName = R.raw.class.getFields();


        for (int i = 0; i < filesName.length; i++) {
            int resourceId = App.getContext().getResources().getIdentifier(filesName[i].getName(), "raw", App.getContext().getPackageName());
            Uri musicUri = Uri.parse("android.resource://" + App.getContext().getPackageName() + "/" + Integer.toString(resourceId)  );

            try {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(App.getContext(), musicUri);
                String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

                if (artist == null) {
                    artist = "";
                }
                if (title == null) {
                    title = "";
                }
                if (album == null) {
                    album = "";
                }

                Song song = new SongBuilder(musicUri, "Donal Trump 2020", "Invalid email")
                                .setArtist(artist).setAlbum(album).setTitle(title).build();


                songsList.add(song);

            } catch (Exception e) {
                Log.e(TAG, "failed to get songs from folder");
            }
        }
        java.util.Collections.sort(songsList, new SongComparator());
    }
    static class SongComparator implements Comparator<Song> {
        @Override
        public int compare(Song a, Song b) {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    }

    /**
     * @precondition songsList cannot be null
     * @postcondition returned list will be filled with albums found in songsList
     * @postcondition returned list will be in alphabetical order of album name
     * @postcondition each album object in the return list will be set the following fields: name, singer, songsInAlbum
     * @postcondition songsInAlbum field of each album object will be filled with songs from that album
     */
    public static ArrayList<Album> getAlbumList(ArrayList<Song> songsList) {
        if (songsList == null) {
            System.err.println("Argument passed into getAlbumList() is null.");
            throw new IllegalArgumentException();
        }

        HashMap<String, Album> albumsMap = new HashMap<String, Album>();

        for ( Song song : songsList) {
            if (!albumsMap.containsKey(song.getAlbum() + song.getArtist())) {
                Album album = new Album(song.getAlbum(), song.getArtist());
                album.getSongsInAlbum().add(song);
                albumsMap.put(song.getAlbum() + song.getArtist(), album);
            } else {
                Album album = albumsMap.get(song.getAlbum() + song.getArtist());
                album.getSongsInAlbum().add(song);
            }
        }

        ArrayList<Album> albumsList = new ArrayList<Album>(albumsMap.values());
        java.util.Collections.sort(albumsList, new AlbumComparator());
        return albumsList;
    }
    static class AlbumComparator implements Comparator<Album> {
        @Override
        public int compare(Album a, Album b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }

    /**
     * Calculate the weight for a song
     * @precondition song can not be null
     * @precondition updateTimeDiffernce() and updateDistance has been called on the song
     * @postcondition the algorithmValue field of song will be set to its weight
     */
    static public void calculateSongWeight(Song song) {
        if (song == null) {
            System.err.println("Argument passed into calculateSongWeight() is null");
            throw new IllegalArgumentException();
        }

        double distFactor = 1.0;
        double timeFactor = 1.0;
        double dayFactor = 1.0;
        double result = 0.0;
        double distance, timeDiff;
        boolean sameTime, sameDay;

        // songs played closer will be in the playlist first
        distance = song.getDistance();

        sameTime = false;
        sameDay = false;

        timeDiff = song.getTimeDifference();
        distFactor = 1.0;
        timeFactor = 1.0;
        dayFactor = 1.0;

        if (distance > 1000) {
            distFactor = 0.0;
        }
        if (!sameTime) {
            timeFactor = 0.0;
        }
        if (!sameDay) {
            dayFactor = 0.0;
        }

        if (distance < 1) {
            distance = 1;
        }

        if (timeDiff < 1) {
            timeDiff = 1;
        }

        result = (1.0 / distance) * 2 * distFactor + (1.0 / timeDiff) * timeFactor + dayFactor;

        song.setAlgorithmValue(result);
    }

    static public double calculateSongWeightVibe(Song song) {
        if (song == null) {
            System.err.println("Argument passed into calculateSongWeight() is null");
            throw new IllegalArgumentException();
        }

        UserManager manager;
        manager = UserManager.getUserManager();
        // priority in queue is based on (a)distance,(b)played in the last week,(c)played by amigo

        double distFactor = 1.0;
        double distPenalty = 0.0;
        double weekFactor = 0.0;
        double friendFactor = 0.0;
        double distanceDiff,timeDiff,result;
        String userEmail;

        distanceDiff = song.getDistance();
        timeDiff = song.getTimeDifference();
        userEmail = song.getEmail();


        // subtract .01 by every 100 feet after 1000
        if (distanceDiff > 1000) {
            distPenalty = (distanceDiff - 1000)/1000;

            // do not allow penalty greater than 1
            if (distPenalty > 1.0) {
                distPenalty = 1.0;
            }

            distFactor -= distPenalty;

        }



        // check if the song was played within the last week
        if (timeDiff < 604800000) {
            weekFactor = 1.0;
        }

        // check if the song was played by your friend
        if (manager.checkIfFriend(userEmail)){
            friendFactor = 1.0;
        }

        // tie breakers
        if (distFactor == weekFactor) weekFactor = .99;
        if (weekFactor == friendFactor) friendFactor = .99;
        if (friendFactor == distFactor) friendFactor = .99;

        result = distFactor + weekFactor + friendFactor;
        song.setAlgorithmValue(result);

        return result;

    }



}
