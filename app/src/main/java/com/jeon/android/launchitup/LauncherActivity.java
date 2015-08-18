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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jeon.android.launchitup.data.LaunchItem;

import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LauncherActivity extends Activity implements View.OnClickListener, FloatingActionMenu.OnMenuToggleListener {

    public static final String PREF_KEY_LAUNCH_DATA_LIST = "pref_key_launch_data_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        Log.d("count:%d", dataList.size());
        if (dataList.isEmpty()) {
            startActivity(new Intent(this, LaunchItemDialogActivity.class));
            finish();
            return;
        }

        if (dataList.size() == 1) {
            for (String data : dataList) {
                try {
                    LaunchItem launchItem = new LaunchItem(data);
                    startActivity(Intent.parseUri(launchItem.getLaunchUriString(), 0));
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
        ViewGroup rootView = (ViewGroup) findViewById(R.id.root_view);
        rootView.setOnClickListener(this);

        generateViews(dataList, rootView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.root_view:
                finish();
                break;
            default:
                LaunchItem launchItem = (LaunchItem) v.getTag();
                if (launchItem != null) {
                    try {
                        startActivity(Intent.parseUri(launchItem.getLaunchUriString(), 0));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
                break;
        }
    }

    @Override
    public void onMenuToggle(boolean opened) {
        Log.d("opened:%b", opened);
        if (!opened) {
            finish();
        }
    }

    private void generateViews(@NonNull Collection<String> items, @NonNull ViewGroup parent) {
        if (items.isEmpty()) {
            Log.e("there is no items.");
            return;
        }

        int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
        LayoutInflater inflater = getLayoutInflater();
        for (String data : items) {
            try {
                View view = inflater.inflate(R.layout.launch_item_layout, parent, false);
                final FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
                parent.addView(view);

                LaunchItem launchItem = new LaunchItem(data);
                button.setTag(launchItem);
                button.setOnClickListener(this);

                Glide.with(this)
                        .load(launchItem.getIconUri())
                        .error(android.R.drawable.ic_menu_help)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(new SimpleTarget<GlideDrawable>(size, size) {

                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                button.setImageDrawable(resource);
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
