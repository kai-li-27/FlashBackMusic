package com.android.flashbackmusic;

import android.app.Application;
import android.content.Context;

/**
 * Created by K on 2/20/2018.
 */

public class App extends Application {
    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }


    public static Context getContext()
    {
        return context;
    }
}
