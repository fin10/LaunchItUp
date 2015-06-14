package com.jeon.android.launchitup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.net.URISyntaxException;

public class LauncherActivity extends Activity {

    public static final String PREF_KEY_LAUNCH_INTENT = "pref_key_launch_intent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = null;
        String uri = prefs.getString(PREF_KEY_LAUNCH_INTENT, null);
        Log.d("intent:%s", uri);
        if (TextUtils.isEmpty(uri)) {
            intent = new Intent(this, AppChooseDialogActivity.class);
        } else {
            try {
                intent = Intent.parseUri(uri, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (intent != null) {
            startActivity(intent);
            Survey.send(Survey.Action.LAUNCH, "application");
        } else {
            Log.e("intent is null.");
        }

        finish();
    }
}
