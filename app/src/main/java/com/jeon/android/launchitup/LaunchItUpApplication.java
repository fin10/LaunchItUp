package com.jeon.android.launchitup;

import android.app.Application;

public class LaunchItUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.init("LaunchItUp");
    }
}
