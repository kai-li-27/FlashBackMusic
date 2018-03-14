package com.android.flashbackmusic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by K on 3/14/2018.
 */

public class DownloadReceiver extends BroadcastReceiver {
    private long download_id;
    private boolean isDownloadedByuser = false;
    private String filePath;
    private String URL = "";
    private String email;



    public DownloadReceiver(boolean isDownloadedByuser, String filePath, String URL, String email) {
        this.filePath = filePath;
        this.URL = URL;
        this.isDownloadedByuser = isDownloadedByuser;
        this.email = email;
    }

    public void setDownload_id(long reference_id) {
        this.download_id = reference_id;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //check if the broadcast message is for our Enqueued download
        long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);


        /* This will return if able to fetch song info from the file. It DOES NOT check for anything else
           So if it is not a valid song but somehow we can fetch the info, it still returns.
         */
        if(referenceId == download_id) {

            Uri fileUri = Uri.fromFile(new File(filePath));

            try {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(App.getContext(), fileUri);
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

                Song song;
                if (isDownloadedByuser) {
                    IUser self = UserManager.getUserManager().getSelf();
                    song = new SongBuilder(fileUri, self.getUserId(), self.getEmail())
                            .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(false).setDownLoadURL(URL).build();
                    VibeDatabase.getDatabase().insertSong(song);
                } else {
                    song =  new SongBuilder(fileUri, "", email) //TODO figure out what userIDstring should be
                            .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(false).setDownLoadURL(URL).build();
                }

                SongManager.getSongManager().newSongDownloaded(song, isDownloadedByuser);

                Toast.makeText(App.getContext(), "Download Completed", Toast.LENGTH_LONG).show();
                return;

            } catch (Exception e) {
                Log.d(TAG, "Downloaded song was not audio file");
            }

            //TODO unzip

            //TODO not valid
        }


            //TODO first check if it's song, if yes, toast show
            //TODO second checkif if it's zip, if yes , upzip and show
            //TODO third everything else, toast not valid audio

    }

    public static String parseFileNameFromURL(String url) {
        String fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
        return  fileName;
    }

}
