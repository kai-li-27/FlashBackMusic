package com.android.flashbackmusic;

// Reference to https://stackoverflow.com/questions/3028306/download-a-file-with-android-
// and-showing-the-progress-in-a-progressdialog for help downloading a file using a url which
// modified for a song and album specifically

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

        Button DownloadMusic = findViewById(R.id.start_download_button);
        DownloadMusic.setOnClickListener(this);

        songUrl = findViewById(R.id.download_url_text);

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

        Toast.makeText(this, "Download has started", Toast.LENGTH_LONG).show();
        String fileName = DownloadReceiver.parseFileNameFromURL(songUrl.getText().toString());
        String fileSubPath = "UserSongs/" + fileName;
        String filePath = getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/" + fileSubPath;

        request.setTitle(fileName);
        request.setDescription(fileName);

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
            } catch (Exception o) {
                o.printStackTrace();
                return;
            }

            DownloadManager downloadManager = (DownloadManager) App.getContext().getSystemService(App.getContext().DOWNLOAD_SERVICE);
            DownloadManager.Request request;
            try {
                request = new DownloadManager.Request(uri);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            String fileName = DownloadReceiver.parseFileNameFromURL(song.getDownloadURL());
            String fileSubPath = "VibeSongs/" + fileName;
            String filePath = App.getContext().getExternalFilesDir(null) + "/" + Environment.DIRECTORY_MUSIC + "/" + fileSubPath;

            request.setTitle(fileName);
            request.setDescription(fileName);
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





