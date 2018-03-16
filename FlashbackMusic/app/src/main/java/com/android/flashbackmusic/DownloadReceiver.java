package com.android.flashbackmusic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
    import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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

        if(referenceId == download_id) {

            Uri fileUri = Uri.fromFile(new File(filePath));

            // determine if it is a zipped file
            if (filePath.toLowerCase().contains(".zip") || filePath.toLowerCase().contains(".rar") || filePath.toLowerCase().contains(".tgz")) {
                fetchFileFromZip();
                return;
            }

            if (!parseSongFromUri(fileUri)) {
                if (isDownloadedByuser) {
                    Toast.makeText(App.getContext(), "Downloaded file is not audio file", Toast.LENGTH_LONG).show();
                }
            } else{
                if (isDownloadedByuser) {
                    Toast.makeText(App.getContext(), "Song downloaded and loaded", Toast.LENGTH_LONG).show();
                }
            }
        }
    }



    private void fetchFileFromZip() {
        String folderPath = App.getContext().getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/" ;

        if (isDownloadedByuser) {
            folderPath += "UserSongs/";
        } else {
            folderPath += "VibeSongs/";
        }

        try {

            FileInputStream is = new FileInputStream(filePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;

            if (isDownloadedByuser) {
                Toast.makeText(App.getContext(), "Unzipping the album", Toast.LENGTH_LONG).show();
            }

            while((ze = zis.getNextEntry()) != null) { //unzipping each item in the zipped file
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;

                String filename = ze.getName();
                FileOutputStream fout = new FileOutputStream(folderPath + filename);

                // reading and writing
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                    byte[] bytes = baos.toByteArray();
                    fout.write(bytes);
                    baos.reset();
                }

                fout.close();
                zis.closeEntry();

                File songFile = new File(folderPath + filename);
                Uri musicUri = Uri.fromFile(songFile);

                parseSongFromUri(musicUri);
            }

            zis.close();
            if (isDownloadedByuser) {
                Toast.makeText(App.getContext(), "Album downloaded and unzipped", Toast.LENGTH_SHORT).show();
            }
            new File(filePath).delete();

        } catch (Exception e) {
            if (isDownloadedByuser) {
                Toast.makeText(App.getContext(), "Downloaded file was not zip file", Toast.LENGTH_LONG).show();
                new File(filePath).delete();
            }
            e.printStackTrace();
        }
    }



    private boolean parseSongFromUri(Uri songUri) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(App.getContext(), songUri);
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
                song = new SongBuilder(songUri, self.getUserId(), self.getEmail())
                        .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(false).setDownLoadURL(URL).build();
                VibeDatabase.getDatabase().insertSong(song);
            } else {
                song =  new SongBuilder(songUri, "", email)
                        .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(false).setDownLoadURL(URL).build();
            }

            SongManager.getSongManager().newSongDownloaded(song, isDownloadedByuser);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            new File(filePath).delete();
            return false;
        }
    }


    public static String parseFileNameFromURL(String url) {
        String fileName = url.substring( url.lastIndexOf('/')+1, url.lastIndexOf('.') + 4);
        return  fileName;
    }

}
