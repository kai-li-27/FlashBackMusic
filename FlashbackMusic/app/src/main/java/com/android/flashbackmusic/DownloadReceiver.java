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

        /* This will return if able to fetch song info from the file. It DOES NOT check for anything else
           So if it is not a valid song but somehow we can fetch the info, it still returns.
         */
        if(referenceId == download_id) {

            Uri fileUri = Uri.fromFile(new File(filePath));

            if (filePath.toLowerCase().contains(".zip") || filePath.toLowerCase().contains(".rar") || filePath.toLowerCase().contains(".tgz")) {
                fetchFileFromZip(fileUri);
                return;
            }

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
                Log.d(TAG, "Downloaded file was not audio file");
            }




        }


            //TODO first check if it's song, if yes, toast show
            //TODO second checkif if it's zip, if yes , upzip and show
            //TODO third everything else, toast not valid audio

    }


    private void fetchFileFromZip(Uri fileUri) {
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

            Toast.makeText(App.getContext(), "Unzipping the album", Toast.LENGTH_LONG).show();

            while((ze = zis.getNextEntry()) != null) {
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
                System.out.println(folderPath + filename);
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

                        Song song;
                        if (isDownloadedByuser) {
                            IUser self = UserManager.getUserManager().getSelf();
                            song = new SongBuilder(musicUri, self.getUserId(), self.getEmail())
                                    .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(true).setDownLoadURL(URL).build();
                            VibeDatabase.getDatabase().insertSong(song);
                        } else {
                            song = new SongBuilder(musicUri, "", email) //TODO figure out what userIDstring should be
                                    .setArtist(artist).setAlbum(album).setTitle(title).setPartOfAlbum(true).setDownLoadURL(URL).build();
                        }

                        SongManager.getSongManager().newSongDownloaded(song, isDownloadedByuser);


                    } catch (Exception e) {
                        Log.d(TAG, "Failed to import '" + songFile.toString() + "'");
                        e.printStackTrace();
                    }



            }
            zis.close();
            Toast.makeText(App.getContext(), "Zip downloaded and unzipped", Toast.LENGTH_SHORT).show();
            new File(filePath).delete();

        } catch (Exception e) {
            Toast.makeText(App.getContext(), "Downloaded file was not zip file", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }


    public static String parseFileNameFromURL(String url) {
        String fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
        return  fileName;
    }

}
