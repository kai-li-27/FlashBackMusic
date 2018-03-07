package com.android.flashbackmusic;


import android.content.Context;

import com.google.android.gms.plus.People;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.List;

/**
 * Created by kwmag on 3/5/2018.
 */

public class ImportGoogleFriends {

    private static final String APPLICATION_NAME = "FlashbackMusic";

    // user credentials storage location
    // private static final java.io.File DATA_STORE_DIR = new java

    // based on https://developers.google.com/people/v1/getting-started

    public static PeopleService setUp(Context context, String serverAuthCode) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        // from Google API Console
        String clientId = context.getString(R.string.clientId);
        String clientSecret = context.getString(R.string.client_secret);

        // might need to change the following 2 lines
        String redirectUrl = context.getString(R.string.redirectUrl);

        GoogleTokenResponse tokenResponses = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory, clientId, clientSecret, serverAuthCode, redirectUrl).execute();

        // create GoogleCredential object
        GoogleCredential credentials = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credentials.setFromTokenResponse(tokenResponses);

        return new PeopleService.Builder(httpTransport, jsonFactory, credentials)
                .setApplicationName("Flashback Music")
                .build();

    }

    public static List<Person> getConnections() {
        return null; // FIXME
    }
}
