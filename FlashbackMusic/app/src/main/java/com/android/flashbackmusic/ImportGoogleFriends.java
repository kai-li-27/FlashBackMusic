package com.android.flashbackmusic;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwmag on 3/5/2018.
 */

public class ImportGoogleFriends implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String APPLICATION_NAME = "FlashbackMusic";
    private static final String TAG = "ImportGoogleFriends";

    private GoogleSignInOptions gso;
    private UserManager userManager;

    public ImportGoogleFriends(MainActivity mainActivity) {
        userManager = UserManager.getUserManager();

        getUserInfo();

        // google sign-in options
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(App.getContext().getString(R.string.clientId))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN),
                        new Scope("https://www.googleapis.com/auth/contacts.readonly"))
                .build();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(App.getContext())
                .enableAutoManage(mainActivity, this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

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
    }


    public GoogleSignInOptions getGso() {
        return gso;
    }

    private void getUserInfo() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(App.getContext());
        if (acct != null) {
            String personEmail = acct.getEmail();
            userManager.addOneUserToList(acct.getDisplayName(), personEmail, "self", null, acct.getId());
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        try {
            GoogleSignInAccount account = result.getSignInAccount();

            new PeoplesAsync().execute(account.getServerAuthCode());

        } catch (Exception e) {
            Log.w(TAG, "handleSignInResult:error", e);
        }
    }

    // based on https://developers.google.com/people/v1/getting-started
    public static PeopleService setUp(Context context, String serverAuthCode) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        // Redirect URL for web based applications.
        // Can be empty too.
        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

        // STEP 1
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                context.getString(R.string.clientId),
                context.getString(R.string.client_secret),
                serverAuthCode,
                redirectUrl).execute();

        // STEP 2
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(context.getString(R.string.clientId), context.getString(R.string.client_secret))
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setFromTokenResponse(tokenResponse);

        // STEP 3
        return new PeopleService.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }




    public void authorizationCodeReceived(String authorizationCode) {
        new PeoplesAsync().execute(authorizationCode);
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
                PeopleService peopleService = ImportGoogleFriends.setUp(App.getContext(), params[0]);

                ListConnectionsResponse response = peopleService.people().connections()
                        .list("people/me")
                        // This line's really important! Here's why:
                        // http://stackoverflow.com/questions/35604406/retrieving-information-about-a-contact-with-google-people-api-java
                        .setRequestMaskIncludeField("person.names,person.emailAddresses")
                        .execute();
                List<Person> connections = response.getConnections();

                if (connections == null) {
                    return nameList;
                }


                for (Person person : connections) {
                    if (!person.isEmpty()) {
                        List<Name> names = person.getNames();
                        List<EmailAddress> emailAddresses = person.getEmailAddresses();
                        String chosenName = null;
                        String chosenEmail = null;


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
                            System.out.println(chosenName + ": " + chosenEmail);
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
