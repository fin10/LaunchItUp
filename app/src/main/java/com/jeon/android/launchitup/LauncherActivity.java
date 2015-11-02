package com.jeon.android.launchitup;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LaunchItemModel;

import java.net.URISyntaxException;
import java.util.List;

public final class LauncherActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<LaunchItem> items = LaunchItemModel.getItemList(this);
        Log.d("count:%d", items.size());
        if (items.isEmpty()) {
            startActivity(new Intent(this, LaunchItemDialogActivity.class));
            finish();
            return;
        }

        if (items.size() == 1) {
            try {
                startActivity(Intent.parseUri(items.get(0).getLaunchUriString(), 0));
            } catch (URISyntaxException | ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.not_found_s, items.get(0).getTitle()), Toast.LENGTH_LONG).show();
            }

            finish();
            return;
        }

        setContentView(R.layout.activity_launcher);
        ViewGroup rootView = (ViewGroup) findViewById(R.id.root_view);
        rootView.setOnClickListener(this);

        setupLaunchIcons(items, rootView);
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
                    } catch (URISyntaxException | ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.not_found_s, launchItem.getTitle()), Toast.LENGTH_LONG).show();
                    }

                    finish();
                }
                break;
        }
    }

    private void setupLaunchIcons(@NonNull List<LaunchItem> items, @NonNull ViewGroup parent) {
        if (items.isEmpty()) {
            Log.e("there is no items.");
            return;
        }

        int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
        LayoutInflater inflater = getLayoutInflater();
        for (LaunchItem item : items) {
            View view = inflater.inflate(R.layout.launch_item_layout, parent, false);
            parent.addView(view);

            TextView titleView = (TextView) view.findViewById(R.id.title_view);
            if (item.isShownTitle()) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(item.getTitle());
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }

            final ImageView button = (ImageView) view.findViewById(R.id.floating_action_button);
            button.setTag(item);
            button.setOnClickListener(this);

            Glide.with(this)
                    .load(item.getIconUri())
                    .error(android.R.drawable.ic_menu_help)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<GlideDrawable>(size, size) {

                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            button.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            button.setImageDrawable(errorDrawable);
                        }
                    });
        }
    }
}
