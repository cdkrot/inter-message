package ru.spbau.intermessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public class Intermessage extends Application implements Application.ActivityLifecycleCallbacks {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static boolean isPaused = false;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        registerActivityLifecycleCallbacks(this);
    }

    public static Context getAppContext() {
        return context;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // Nothing to do
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Nothing to do
    }

    @Override
    public void onActivityResumed(Activity activity) {
        isPaused = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isPaused = true;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Nothing to do
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        // Nothing to do
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Nothing to do
    }
}
