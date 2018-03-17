package com.android.flashbackmusic;

import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by K on 3/8/2018.
 */

public class SongManager {
    private ArrayList<Song> listOfAllUserSongs = new ArrayList<>();
    private ArrayList<Song> currentPlayList = new ArrayList<>();
    private ArrayList<Album> listOfAlbums = new ArrayList<>();
    private ArrayList<Song> vibeSongList = new ArrayList<>();
    private HashMap<String, Album> albumsMap = new HashMap<String, Album>();

    private static SongManager instance;

    private String userFoler;
    private String vibeFoler;

    private Location currentlocation = null;
    private static final String TAG = "SongManager"; //for adding a new song

    enum SortMode {
        TITLE, ALBUM, ARTIST, MOST_RECENT, PREFERENCE
    }


    private SongManager() {

        userFoler = App.getContext().getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/UserSongs";
        vibeFoler = App.getContext().getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/VibeSongs";

        Algorithm.importSongsFromResource(listOfAllUserSongs);
        listOfAllUserSongs.get(0).setDownloadURL("https://www.dropbox.com/s/ilvs4t50l2rxxzz/spiraling-stars.mp3?dl=1");
        importSongsFromFolder(listOfAllUserSongs, userFoler);
        importSongsFromFolder(listOfAllUserSongs, vibeFoler);
        getAlbumsFromImportedSongs();
        currentPlayList = new ArrayList<>(listOfAllUserSongs);

        if (UserManager.getUserManager().getSelf() == null) { //NOTE: because the checking of google sign-in is executed before this, so this will always work
            Toast.makeText(App.getContext(), "You are not signed in, your play history won't be stored", Toast.LENGTH_LONG).show(); //This will make unit test fails. comment this out before unit test
        } else {
            String userId = UserManager.getUserManager().getSelf().getUserId();
            String userEmail = UserManager.getUserManager().getSelf().getEmail();
            for (Song song : listOfAllUserSongs) {
                song.setUserIdString(userId);
                song.setEmail(userEmail);
            }

            VibeDatabase.getDatabase().upateInfoOfSongsOfUser(listOfAllUserSongs); //This will go to server and get the preference, location and time for each song
        }

        sortByDefault();

    }


    public Song isSongDownloaded(Song song) {
        for (Song i : listOfAllUserSongs) {
            if (i.getTitle().equals(song.getTitle())  && i.getAlbum().equals(song.getAlbum()) && i.getArtist().equals(song.getArtist())) {
                return i;
            }
        }

        return null;
    }





//region getters
    public static SongManager getSongManager() {
        if (instance == null) {
            instance = new SongManager();
        }
        return instance;
    }

    public ArrayList<Album> getAlbumList() {
        return listOfAlbums;
    }

    public ArrayList<Song> getCurrentPlayList() {
        return currentPlayList;
    }

    public ArrayList<Song> getDisplaySongList() {
        return listOfAllUserSongs;
    }

    public ArrayList<Song> getVibeSongList() { return vibeSongList;}
//endregion;





//region Sorting Methods
    public void sortByTitle(){
        for (int i = 0; i < listOfAllUserSongs.size(); i++) { //go through whole list, finds largest each time
            Song temp = listOfAllUserSongs.get(i);
            for (int j = i + 1; j < listOfAllUserSongs.size(); j++) {
                if (temp.getTitle().compareTo(listOfAllUserSongs.get(j).getTitle()) < 0) {
                    temp = listOfAllUserSongs.get(j);
                }
            }
            listOfAllUserSongs.remove(temp);
            listOfAllUserSongs.add(0, temp);//insert the largest to the front
        }
    }


    void sortByAlbum() {
        listOfAllUserSongs.clear(); //TODO make sure that songs in each album is sorted
        for (Album i : listOfAlbums) {
            listOfAllUserSongs.addAll(i.getSongsInAlbum());
        }
    }


    void sortByArtist() {
        sortByTitle();
        for (int i = 0; i < listOfAllUserSongs.size() - 1; i++) { // bubble sort
            for (int j = 0; j < listOfAllUserSongs.size() - 1; j++) {
                if (listOfAllUserSongs.get(j).getArtist().compareTo(listOfAllUserSongs.get(j+1).getArtist()) > 0) {
                    Song temp = listOfAllUserSongs.get(j);
                    listOfAllUserSongs.remove(temp);
                    listOfAllUserSongs.add(j+1, temp);
                }
            }
        }
    }


    void sortByDefault() {
        sortByTitle();
        for (int i = 0; i < listOfAllUserSongs.size(); i++) { //go through whole list, finds least recent each time
            Song temp = listOfAllUserSongs.get(i);
            for (int j = i + 1; j < listOfAllUserSongs.size(); j++) {
                if (temp.getLastTime().compareTo(listOfAllUserSongs.get(j).getLastTime()) >= 0) {
                    temp = listOfAllUserSongs.get(j);
                }
            }
            listOfAllUserSongs.remove(temp);
            listOfAllUserSongs.add(0, temp);//insert the least recent to the front
        }
    }


    void sortByFavorites() {
        sortByTitle();
        for (int i = 0; i < listOfAllUserSongs.size() - 1; i++) { //bubble sort
            for (int j = 0; j < listOfAllUserSongs.size() - 1; j++) {
                if (listOfAllUserSongs.get(j).getPreference() < listOfAllUserSongs.get(j + 1).getPreference()) {
                    Song temp = listOfAllUserSongs.get(j);
                    listOfAllUserSongs.remove(temp);
                    listOfAllUserSongs.add(j+1, temp);
                }
            }
        }
    }
//endregion;





//region Handle playlist change
    public void singleSongChosen() {
        currentPlayList.clear();
        currentPlayList.addAll(listOfAllUserSongs);
    }

    public void updateVibePlaylist(Location location) {
        currentlocation = location;

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
        if (acct == null) {
            return;
        }

        Log.d(TAG, "Yoooooooooo! Location has changed.");
        vibeSongList.clear();
        VibeDatabase.getDatabase().queryByLocationOfAllSongs(location, 1000, vibeSongList);
    }

    public void contactsHaveLoad() {
        if (currentlocation != null) {
            updateVibePlaylist(currentlocation);
        }
    }

    public void albumChosen(int indexOfAlbum) {
        currentPlayList.clear();
        currentPlayList.addAll(listOfAlbums.get(indexOfAlbum).getSongsInAlbum());
    }
//endregion;





//region Import songs from folder
    private void importSongsFromFolder(ArrayList<Song> songsList, String folderPath) {
        if (songsList == null) {
            System.err.println("Argument passed into importSongsFromResource() is null.");
            throw new IllegalArgumentException();
        }

        File folder = new File(folderPath);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File songFile = files[i];
                Uri musicUri = Uri.fromFile(songFile);

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

                    Song song = new SongBuilder(musicUri, "default", "default")
                            .setArtist(artist).setAlbum(album).setTitle(title).build();

                    songsList.add(song);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }



    private void getAlbumsFromImportedSongs() {
        HashMap<String, Album> albumsMap = new HashMap<String, Album>();

        for ( Song song : listOfAllUserSongs) {
            if (!albumsMap.containsKey(song.getAlbum() + song.getArtist())) {
                Album album = new Album(song.getAlbum(), song.getArtist());
                album.getSongsInAlbum().add(song);
                albumsMap.put(song.getAlbum() + song.getArtist(), album);
            } else {
                Album album = albumsMap.get(song.getAlbum() + song.getArtist());
                album.getSongsInAlbum().add(song);
            }
        }

        listOfAlbums = new ArrayList<Album>(albumsMap.values());
        java.util.Collections.sort(listOfAlbums, new AlbumComparator());
    }
    class AlbumComparator implements Comparator<Album> {
        @Override
        public int compare(Album a, Album b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }



    public void newSongDownloaded(Song song, boolean wasDownloadedByUser) {
        if (wasDownloadedByUser) {
            listOfAllUserSongs.add(song);

            if (albumsMap.containsKey(song.getAlbum())) {
                Album album = albumsMap.get(song.getAlbum());
                for (int i = 0; i < album.getSongsInAlbum().size(); i++) { //sort it
                    if (song.getTitle().compareTo(album.getSongsInAlbum().get(i).getTitle()) < 0) {
                        album.getSongsInAlbum().add(i, song);
                        break;
                    }
                }
            } else {
                Album album = new Album(song.getAlbum(), song.getArtist());
                albumsMap.put(song.getAlbum(), album);
                if (listOfAlbums.size() == 0) {
                    listOfAlbums.add(album);
                }
                for (int i = 0; i < listOfAlbums.size(); i++) {
                    if (album.getName().compareTo(listOfAlbums.get(i).getName()) < 0) {
                        listOfAlbums.add(i, album);
                        break;
                    }
                }
                album.getSongsInAlbum().add(song);
            }

        }

        else { //song downloaded for vibe mode
            listOfAllUserSongs.add(song);
            for (Song i : vibeSongList) {
                if (i.getTitle().equals(song.getTitle()) && i.getArtist().equals(song.getArtist()) && i.getAlbum().equals(song.getAlbum())) {
                    i.setUri(song.getUri());
                }
            }
        }
    }
//endregion;

}
