package com.android.flashbackmusic;

import android.app.Application;
import android.content.Context;

/**
 * Created by K on 2/20/2018.
 */

public class App extends Application {
    private static Context context;
    private static SongDao songDao;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        songDao = SongDatabase.getSongDatabase(getApplicationContext()).songDao();
    }

    public static SongDao getSongDao() {
        return songDao;
    }

    public static Context getContext()
    {
        return context;
    }
}
