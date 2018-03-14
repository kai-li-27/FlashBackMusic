package com.android.flashbackmusic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DownloadSong extends AppCompatActivity implements View.OnClickListener {

    private DownloadManager downloadManager;
    private EditText songUrl;
    private IntentFilter filter;

    private final String TAG = "DownloadSogn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dowload_song);


        //Download Music from URL
        Button DownloadMusic = (Button) findViewById(R.id.start_download_button);
        DownloadMusic.setOnClickListener(this);

        songUrl = (EditText) findViewById(R.id.download_url_text);


        //set filter to only when download is complete and register broadcast receiver
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R., menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //Download Music
            case R.id.start_download_button:
                Uri music_uri = Uri.parse(songUrl.getText().toString());
                DownloadData(music_uri, v);
                break;


        }
    }


    private void Check_Music_Status(long Music_DownloadId) {

        DownloadManager.Query MusicDownloadQuery = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        MusicDownloadQuery.setFilterById(Music_DownloadId);

        //Query the download manager about downloads that have been requested.
        Cursor cursor = downloadManager.query(MusicDownloadQuery);
        if (cursor.moveToFirst()) {
            DownloadStatus(cursor, Music_DownloadId);
        }

    }

    private void DownloadStatus(Cursor cursor, long DownloadId) {

        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //get the download filename
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String filename = cursor.getString(filenameIndex);

        String statusText = "";
        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename:\n" + filename;
                break;
        }

        /*
        if(DownloadId == Music_DownloadId) {

            Toast toast = Toast.makeText(DownloadSong.this,
                    "Music Download Status:" + "\n" + statusText + "\n" +
                            reasonText,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();

        }
        */

    }

    private void DownloadData(Uri uri, View v) {

        long downloadReference;

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Provided URL is not valid", Toast.LENGTH_LONG).show();
            return;
        }

        String fileName = DownloadReceiver.parseFileNameFromURL(songUrl.getText().toString());
        String fileSubPath = "UserSongs/" + fileName;
        String filePath = getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/" + fileSubPath;

        //Setting title of request
        request.setTitle(fileName);

        //Setting description of request
        request.setDescription(fileName);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        if (v.getId() == R.id.start_download_button)
            request.setDestinationInExternalFilesDir(DownloadSong.this, Environment.DIRECTORY_MUSIC, fileSubPath);

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);


        DownloadReceiver downloadReceiver = new DownloadReceiver(true, filePath, songUrl.getText().toString(), null);
        downloadReceiver.setDownload_id(downloadReference);
        registerReceiver(downloadReceiver, filter);
    }


    static public class DownLoader {
        public void downloadSongForVibe(Song song) {
            long downloadReference;
            Uri uri;
            try {
                uri = Uri.parse(song.getDownloadURL());
            } catch (Exception o) {return;}
            DownloadManager downloadManager = (DownloadManager) App.getContext().getSystemService(App.getContext().DOWNLOAD_SERVICE);
            DownloadManager.Request request;
            try {
                request = new DownloadManager.Request(uri);
            } catch (Exception e) {
                //Log.d(TAG, "Fuck, can not download this song in vibe mode");
                return;
            }

            String fileName = DownloadReceiver.parseFileNameFromURL(song.getDownloadURL());
            String fileSubPath = "VibeSongs/" + fileName;
            String filePath = App.getContext().getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/" + fileSubPath;

            //Setting title of request
            request.setTitle(fileName);

            //Setting description of request
            request.setDescription(fileName);

            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalFilesDir(App.getContext(), Environment.DIRECTORY_MUSIC, fileSubPath);

            //Enqueue download and save the referenceId
            downloadReference = downloadManager.enqueue(request);


            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);


            DownloadReceiver downloadReceiver = new DownloadReceiver(false, filePath, song.getDownloadURL(), song.getEmail());
            downloadReceiver.setDownload_id(downloadReference);
            App.getContext().registerReceiver(downloadReceiver, filter);
        }
    }
}





