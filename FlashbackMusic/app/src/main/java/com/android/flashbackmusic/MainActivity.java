package com.android.flashbackmusic;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.Toast;


import com.android.flashbackmusic.SongService.MusicBinder;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Landing page with all the songs and albums
 */
public class MainActivity extends AppCompatActivity implements VibeDatabaseEventListener, SongServiceEventListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    private SongService songsService;
    private Intent playIntent;

    private ListView songsView;
    private SongListAdapter songAdapt;
    private AlbumListAdapter albumAdapt;

    private Spinner sortOptions;
    private ArrayAdapter<CharSequence> sortSpinnerAdapter;


    private static final String TAG = "MainActivity";

    private final int RC_SIGN_IN = 42069;
    private final int  RC_API_CHECK = 1;
    private String accountId = "";

    GoogleApiClient mGoogleApiClient;
    UserManager userManager = UserManager.getUserManager();

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


        //Binds with music player
        if (playIntent == null) {
            playIntent = new Intent(this, SongService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }

        // Ask for all the permissions
        getPermissions();
        getUserInfo();


        Switch mySwitch = findViewById(R.id.flashback_switch);
        // google sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.clientId))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN),
                        new Scope("https://www.googleapis.com/auth/contacts.readonly"))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            handleSignInResult(pendingResult.get());
        } else {
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
                if (acct != null) {
                    String personEmail = acct.getEmail();
                    Toast.makeText(App.getContext(), "You have already logged in as: " + personEmail, Toast.LENGTH_LONG).show();
                    return;
                }
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

    private void handleSignInResult(GoogleSignInResult result) {
        try {
            GoogleSignInAccount account = result.getSignInAccount();

            new PeoplesAsync().execute(account.getServerAuthCode());

        } catch (Exception e) {
            Log.w(TAG, "handleSignInResult:error", e);
        }
    }

    private void getUserInfo() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
        if (acct != null) {
            String personEmail = acct.getEmail();
            userManager.addOneUserToList(acct.getDisplayName(), personEmail, "self", null, acct.getId());
        }

    }

    // Don't know if we need to use a google sign-in

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Result received");

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            System.out.println("Status Code: " + result.getStatus());
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                getUserInfo();
                System.out.println("Login succeeded");
                Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());
                // This is what we need to exchange with the server.
                System.out.println(acct.getServerAuthCode());
                new PeoplesAsync().execute(acct.getServerAuthCode());
            } else {
                Toast.makeText(App.getContext(), "Login failed. Error code: " + result.getStatus().toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onRestart() {
        super.onRestart();
        flashbackSwitchOff();
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
     * reset flashback switch UI display
     */
    public void flashbackSwitchOff() {
        final Switch mySwitch = findViewById(R.id.flashback_switch);
        mySwitch.setOnCheckedChangeListener(null);
        mySwitch.setChecked(false);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.v(TAG, "Flashback mode toggled");
                if (checked && !songsService.getFlashBackMode()) {
                    songsService.switchMode(checked);

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

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            songsService = binder.getService();
            songsService.addSongServiceEventListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
//endregion;





class PeoplesAsync extends AsyncTask<String, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //updateUI();

        }

        @Override
        protected List<String> doInBackground(String... params) {

            List<String> nameList = new ArrayList<>();

            try {
                PeopleService peopleService = ImportGoogleFriends.setUp(MainActivity.this, params[0]);

                ListConnectionsResponse response = peopleService.people().connections()
                        .list("people/me")
                        // This line's really important! Here's why:
                        // http://stackoverflow.com/questions/35604406/retrieving-information-about-a-contact-with-google-people-api-java
                        .setRequestMaskIncludeField("person.names,person.emailAddresses")
                        .execute();
                List<Person> connections = response.getConnections();

                if (connections == null) {
                    Toast.makeText(App.getContext(), "Oops, No contacts found. Try to make some friends.", Toast.LENGTH_LONG).show();
                }

                Log.v(TAG, "got to doInBackground line 490");

                for (Person person : connections) {
                    if (!person.isEmpty()) {
                        List<Name> names = person.getNames();
                        List<EmailAddress> emailAddresses = person.getEmailAddresses();
                        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
                        String chosenName = null;
                        String chosenEmail = null;

                        if (phoneNumbers != null)
                            for (PhoneNumber phoneNumber : phoneNumbers)
                                Log.d(TAG, "phone: " + phoneNumber.getValue());

                        if (emailAddresses != null) {
                            boolean gotIt = false;
                            for (EmailAddress emailAddress : emailAddresses) {
                                if (!gotIt) {
                                    chosenEmail = emailAddress.getValue();
                                    gotIt = true;
                                }
                                Log.d(TAG, "email: " + emailAddress.getValue());
                            }
                        }

                        if (names != null) {
                            boolean gotIt = false;
                            for (Name name : names) {
                                if (!gotIt) {
                                    chosenName = name.getDisplayName();
                                    gotIt = true;
                                }
                                nameList.add(name.getDisplayName());
                            }
                        }

                        if (chosenName != null && chosenEmail != null) {
                            // FIXME: change this so that the song list is being passed onto IUser
                            userManager.addOneUserToList(chosenName, chosenEmail, "friend", null, "");
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return nameList;
        }


        @Override
        protected void onPostExecute(List<String> nameList) {
            super.onPostExecute(nameList);
        }
    }


}
