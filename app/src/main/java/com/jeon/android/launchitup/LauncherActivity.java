package com.jeon.android.launchitup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.data.AppData;

import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LauncherActivity extends Activity implements View.OnClickListener {

    public static final String PREF_KEY_LAUNCH_DATA_LIST = "pref_key_launch_data_list";

    private static final int REQUEST_CODE = 1;

    private ViewGroup mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        Log.d("count:%d", dataList.size());
        if (dataList.isEmpty()) {
            startActivity(new Intent(this, AppChooseDialogActivity.class));
            finish();
            return;
        }

        if (dataList.size() == 1) {
            for (String data : dataList) {
                try {
                    AppData appData = new AppData(data);
                    startActivity(Intent.parseUri(appData.getLaunchUriString(), 0));
                    Survey.send(Survey.Action.LAUNCH, "application");
                    break;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            finish();
            return;
        }

        setContentView(R.layout.activity_launcher);
        mRootView = (ViewGroup) findViewById(R.id.root_view);
        generateViews(dataList, mRootView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode:%d, resultCode:%d", requestCode, resultCode);
        if (requestCode != REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            finish();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        Log.d("count:%d", dataList.size());
        if (!dataList.isEmpty()) {
            mRootView.removeAllViews();
            generateViews(dataList, mRootView);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        AppData appData = (AppData) v.getTag();
        if (appData == null) {
            startActivityForResult(new Intent(this, AppChooseDialogActivity.class), REQUEST_CODE);
        } else {
            try {
                startActivity(Intent.parseUri(appData.getLaunchUriString(), 0));
                Survey.send(Survey.Action.LAUNCH, "application");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            finish();
        }
    }

    private void generateViews(@NonNull Collection<String> items, ViewGroup parent) {
        if (items.isEmpty()) {
            Log.e("there is no items.");
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        for (String data : items) {
            try {
                View view = inflater.inflate(R.layout.launch_item, parent, false);

                TextView titleView = (TextView) view.findViewById(R.id.title_text);
                ImageView imageView = (ImageView) view.findViewById(R.id.icon_view);

                AppData appData = new AppData(data);
                titleView.setText(appData.getTitle());
                Glide.with(this)
                        .load(appData.getIconUri())
                        .error(android.R.drawable.ic_menu_help)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);

                view.setTag(appData);
                view.setOnClickListener(this);
                parent.addView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        View view = inflater.inflate(R.layout.launch_item, mRootView, false);
        parent.addView(view);

        ImageView imageView = (ImageView) view.findViewById(R.id.icon_view);

        Glide.with(this)
                .load(R.mipmap.ic_add_black_48dp)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);

        view.setOnClickListener(this);
    }
}
