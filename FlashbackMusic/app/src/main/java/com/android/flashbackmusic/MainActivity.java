package com.android.flashbackmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.android.flashbackmusic.SongService.MusicBinder;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;


/**
 * Landing page with all the songs and albums
 */
public class MainActivity extends AppCompatActivity implements VibeDatabaseEventListener, SongServiceEventListener {


    private SongService songsService;
    private Intent playIntent;

    private ListView songsView;
    private SongListAdapter songAdapt;
    private AlbumListAdapter albumAdapt;

    private Spinner sortOptions;
    private ArrayAdapter<CharSequence> sortSpinnerAdapter;

    private ImportGoogleFriends importGoogleFriends;


    private static final String TAG = "MainActivity";

    private final int RC_SIGN_IN = 420;




//region Methods extended from activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Binds with music player
        if (playIntent == null) {
            playIntent = new Intent(this, SongService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }


        // Ask for all the permissions
        getPermissions();


        // Check if user is signed in
        importGoogleFriends = new ImportGoogleFriends(this);


        final Switch mySwitch = findViewById(R.id.flashback_switch);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.v(TAG, "Flashback mode toggled");
                if (checked && !songsService.getFlashBackMode()) {
                    songsService.switchMode(checked);
                    mySwitch.setChecked(false);

                    if (songsService.getFlashBackMode() ) {
                        Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                        intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                        startActivity(intent);
                    }

                    else {
                        mySwitch.setChecked(false);
                    }

                } else if (checked && songsService.getFlashBackMode()) {
                    Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                    intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                    startActivity(intent);
                }
            }
        });


        // Google sign-in button
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, importGoogleFriends.getGso());
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
                if (acct != null) {
                    String personEmail = acct.getEmail();
                    Toast.makeText(App.getContext(), "You have already signed in as: " + personEmail, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                Log.i(TAG, "clicked to sign in");
            }
        });


        Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override // TODO Get switching working
            public void onClick(View view) {
                if (UserManager.getUserManager().getSelf() == null) {
                    Toast.makeText(App.getContext(), "Downloaded feature is not supported unless you log in", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent1 = new Intent(App.getContext(), DownloadSong.class);
                startActivity(intent1);
            }
        });


        Button setDateTime = findViewById(R.id.set_temporal_button);
        setDateTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(App.getContext(), SetAppTimeActivity.class);
                startActivity(intent);
            }

        });


        TextView appTimeText = findViewById(R.id.flashback_time_text);
        appTimeText.setText("Flashback Time: " + TimeAndDate.getTimeAndDate().toString());


        songAdapt = new SongListAdapter(this, SongManager.getSongManager().getDisplaySongList());
        albumAdapt = new AlbumListAdapter(this, SongManager.getSongManager().getAlbumList());
        songsView = findViewById(R.id.song_list);
        songsView.setAdapter(songAdapt);


        // set the sorting options available for the sort options
        sortOptions = (Spinner) findViewById(R.id.sortingOptions);
        sortSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sortOptions, android.R.layout.simple_spinner_item);
        sortSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOptions.setAdapter(sortSpinnerAdapter);
        sortOptions.setOnItemSelectedListener(new SortSongsOptionListener(songAdapt));


        // Display the songs
        TabLayout tabLayout = findViewById(R.id.topTabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.v(TAG, "tab was selected");
                String theTab = tab.getText().toString();
                TextView sortSongsText = (TextView) findViewById(R.id.sortOptionsText);
                Spinner sortOptions = (Spinner) findViewById(R.id.sortingOptions);
                if (theTab.equalsIgnoreCase("songs")) {
                    songsView.setAdapter(songAdapt);
                    sortSongsText.setVisibility(View.VISIBLE);
                    sortOptions.setVisibility(View.VISIBLE);
                }
                if (theTab.equalsIgnoreCase("albums")) {
                    songsView.setAdapter(albumAdapt);
                    sortSongsText.setVisibility(View.GONE);
                    sortOptions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    /**
     * Override back button to not kill this activity
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        stopService(playIntent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            System.out.println("Status Code: " + result.getStatus());
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                String personEmail = acct.getEmail();
                UserManager.getUserManager().addOneUserToList(acct.getDisplayName(), personEmail, "self", null, acct.getId());
                Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());
                // This is what we need to exchange with the server.
                System.out.println(acct.getServerAuthCode());
                importGoogleFriends.authorizationCodeReceived(acct.getServerAuthCode());
            } else {
                Log.d(TAG, "Login failed. Error code: " + result.getStatus().toString());
            }
        }
    }


    @Override
    public void onRestart() {
        super.onRestart();
        TextView appTimeText = findViewById(R.id.flashback_time_text);
        appTimeText.setText("Flashback Time: " + TimeAndDate.getTimeAndDate().toString());
    }
//endregion;





//region Other methods of MainActivity
    /**
     * Gets Location permission from user if did not get it yet
     */
    public void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
    }


    /**
     * When a song is clicked, play the song
     * @param view
     */
    public void chosenSong(View view) {
        Log.v(TAG, "selected a song to play");
        SongManager.getSongManager().singleSongChosen();
        Intent intent = new Intent(this, IndividualSong.class);
        intent.putExtra(Intent.EXTRA_INDEX,(int)view.getTag()); //view.getTage() returns the index of the song in the displayed list
        startActivity(intent);
    }


    /**
     * When an album is clicked, set the playlist to that album and play the first song in the album
     * @param view
     */
    public void chosenAlbum(View view) {
        Log.v(TAG, "selected an album to play");
        SongManager.getSongManager().albumChosen((int)view.getTag());
        Intent intent = new Intent(this, IndividualSong.class);
        Log.v(TAG, "added songs in album to queue");
        intent.putExtra(Intent.EXTRA_INDEX, 0); //0 means to play the first song
        startActivity(intent);
    }


    /**
     * Bind with the music service
     */
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            songsService = binder.getService();
            songsService.addSongServiceEventListener(MainActivity.this);
            if (songsService.getFlashBackMode()) {
                Switch mySwitch = findViewById(R.id.flashback_switch);
                mySwitch.setChecked(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
//endregion;





//region SongServiceEventListener handlers
    @Override
    public void onSongLoaded(Song loadedSong) {

    }

    @Override
    public void onSongCompleted(Song completedSong, Song nextSong) {

    }

    @Override
    public void onSongPaused(Song currentSong) {

    }

    @Override
    public void onSongSkipped(Song skippedSong, Song nextSong) {

    }

    @Override
    public void onSongResumed(Song currentSong) {

    }

    @Override
    public void onVibeModeToggled(boolean vibeModeOn) {

    }
//endregion;





//region VibeDatabaseEventListener Handler
    @Override
    public void onConnectionChanged(boolean connected) {

    }
//endregion;
}
