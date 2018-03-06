package com.android.flashbackmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.android.flashbackmusic.SongsService.MusicBinder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * Landing page with all the songs and albums
 */
public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> listOfAllSongs = new ArrayList<Song>();
    private ArrayList<Song> currentPlayList = new ArrayList<Song>();
    private ArrayList<Album> albumsList;

    private boolean didChooseAlbum = true; //set to true because on start current playlist is empty and needs to be populated
    private boolean isMusicBound = false;

    private Intent playIntent;
    private ListView songsView;
    private SongListAdapter songAdapt;
    private AlbumListAdapter albumAdapt;

    private SongsService songsService;

    private static final String TAG = "MainActivity";

    private final int RC_SIGN_IN = 42069;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Load all the songs
        listOfAllSongs = new ArrayList<Song>();
        currentPlayList = new ArrayList<Song>();
        albumsList = new ArrayList<Album>();
        Algorithm.importSongsFromResource(listOfAllSongs);
        albumsList = Algorithm.getAlbumList(listOfAllSongs);

        //Binds with music player
        if (playIntent == null) {
            playIntent = new Intent(this, SongsService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }

        // Ask for location permission
        getPermissions();

        // google sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                Log.i(TAG, "clicked to sign in");
            }
        });

        /* final Button signButton = findViewById(R.id.sign_in_button);

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
        */


        Switch mySwitch = (Switch) findViewById(R.id.flashback_switch);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.v(TAG, "Flashback mode toggled");
                if (checked && !songsService.getFlashBackMode()) {
                    songsService.switchMode();
                    Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                    intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                    startActivity(intent);
                } else if (checked && songsService.getFlashBackMode()) {
                    Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                    intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                    startActivity(intent);
                }
            }
        });

        // Display the songs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.topTabs);
        songAdapt = new SongListAdapter(this, listOfAllSongs);
        albumAdapt = new AlbumListAdapter(this, albumsList);
        songsView = (ListView) findViewById(R.id.song_list);
        songsView.setAdapter(songAdapt);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.v(TAG, "tab was selected");
                String theTab = tab.getText().toString();
                if (theTab.equalsIgnoreCase("songs")) {
                    songsView.setAdapter(songAdapt);
                }
                if (theTab.equalsIgnoreCase("albums")) {
                    songsView.setAdapter(albumAdapt);
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

    // Don't know if we need to use a google sign-in

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account) {
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


    @Override
    public void onRestart() {
        super.onRestart();
        flashbackSwitchOff();
    }

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
     * reset flashback switch UI display
     */
    public void flashbackSwitchOff() {
        Switch mySwitch = (Switch) findViewById(R.id.flashback_switch);
        mySwitch.setOnCheckedChangeListener(null);
        mySwitch.setChecked(false);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.v(TAG, "Flashback mode toggled");
                if (checked && !songsService.getFlashBackMode()) {
                    songsService.switchMode();
                    Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                    intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                    startActivity(intent);
                } else if (checked && songsService.getFlashBackMode()) {
                    Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                    intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * When a song is clicked, play the song
     * @param view
     */
    public void chosenSong(View view) {
        Log.v(TAG, "selected a song to play");
        Intent intent = new Intent(this, IndividualSong.class);
        intent.putExtra(Intent.EXTRA_INDEX,(int)view.getTag()); //view.getTage() returns the index of the song in the displayed list
        if (didChooseAlbum) {
            Log.v(TAG, "selected album");
            currentPlayList.clear();
            for (Song i : listOfAllSongs) {
                currentPlayList.add(i);
            }
            didChooseAlbum = false;
        }
        startActivity(intent);
    }

    /**
     * When an album is clicked, set the playlist to that album and play the first song in the album
     * @param view
     */
    public void chosenAlbum(View view) {
        Log.v(TAG, "selected an album to play");
        Intent intent = new Intent(this, IndividualSong.class);
        Album currAlbum = albumsList.get((int)view.getTag());
        didChooseAlbum = true;
        currentPlayList.clear();
        for (Song i : currAlbum.getSongsInAlbum()) {
            currentPlayList.add(i);
        }
        Log.v(TAG, "added songs in album to queue");
        intent.putExtra(Intent.EXTRA_INDEX, 0); //0 means to play the first song
        startActivity(intent);
    }

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            songsService = binder.getService();
            songsService.setList(currentPlayList);
            songsService.setListOfAllSongs(listOfAllSongs);
            songsService.setMainActivity(MainActivity.this);
            isMusicBound = true;

            SharedPreferences flashback_state = getSharedPreferences("FlashBackMode_State", MODE_PRIVATE);
            if (flashback_state.getBoolean("State",false)) {
                songsService.switchMode();
                Intent intent = new Intent(MainActivity.this, IndividualSong.class);
                intent.putExtra(Intent.EXTRA_INDEX,0); // This does nothing, just it keeps it from crashing
                startActivity(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };


    /**
     * Change the color of background based on on/off of flashback mode
     */
    public void changeBackgroundForFlashback() {
        Log.v(TAG, "changing background to indicate flashback mode");
        if (songsService == null) {
            Log.e("changeBackground", "songsServices is null");
            return;
        }

        Switch mySwitch = (Switch) findViewById(R.id.flashback_switch);
        final ConstraintLayout indivSongActivity = (ConstraintLayout) findViewById(R.id.MainActivity);

        if (songsService.getFlashBackMode() && mySwitch.isChecked()) {
            indivSongActivity.setBackgroundColor(Color.parseColor("#f2d5b8"));
        } else if (!songsService.getFlashBackMode() && mySwitch.isChecked()) {
            mySwitch.setChecked(false);
            indivSongActivity.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else if (!songsService.getFlashBackMode() && !mySwitch.isChecked()) {
            indivSongActivity.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            mySwitch.setChecked(true);
            indivSongActivity.setBackgroundColor(Color.parseColor("#f2d5b8"));
        }
    }




}
