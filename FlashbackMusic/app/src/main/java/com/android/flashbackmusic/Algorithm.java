package com.android.flashbackmusic;

import android.app.Application;
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
            System.err.println("Argument to importSongsFromResource can't be null.");
            throw new IllegalArgumentException();
        }
        if (songsList.size() != 0) {
            System.err.println("Argument to importSongsFromResource should be empty.");
            throw new IllegalArgumentException();
        }
        Field[] filesName = R.raw.class.getFields();
        SongDao songDao = App.getSongDao();

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

                Song song = new Song(title, artist, album, songDao);
                if (songDao.isIntheDB(title, artist, album) == null) {
                    songDao.insertSong(song);
                }
                song.uri = musicUri;
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
            System.err.println("Argument to importSongsFromResource can't be null.");
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
}
