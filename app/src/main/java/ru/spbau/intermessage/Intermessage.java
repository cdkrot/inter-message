package ru.spbau.intermessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public class Intermessage extends Application implements Application.ActivityLifecycleCallbacks {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static boolean paused = false;
    private static boolean invalidated = false;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        registerActivityLifecycleCallbacks(this);
    }

    public static Context getAppContext() {
        return context;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static boolean invalidated() {
        boolean result = invalidated;
        invalidated = false;
        return result;
    }

    public static void invalidateMessages() {
        invalidated = true;
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
        paused = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;
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
