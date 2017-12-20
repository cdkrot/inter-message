package ru.spbau.intermessage;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class Intermessage extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Intermessage.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Intermessage.context;
    }
}
