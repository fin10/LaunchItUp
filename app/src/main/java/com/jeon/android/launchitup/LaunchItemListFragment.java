package com.jeon.android.launchitup;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LaunchItemModel;

import java.util.List;

public final class LaunchItemListFragment extends Fragment implements LaunchItemModel.EventListener, View.OnClickListener {

    private ViewGroup mLayout;

    @NonNull
    private static View createAppIcon(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull LaunchItem data, @NonNull ViewGroup parent) {
        View layout = inflater.inflate(R.layout.checked_app_item_layout, parent, false);
        ImageView appIcon = (ImageView) layout.findViewById(R.id.app_icon_image_view);

        Glide.with(context)
                .load(data.getIconUri())
                .error(android.R.drawable.ic_menu_help)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(appIcon);

        return layout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_launch_item_list, container, false);
        mLayout = (ViewGroup) root.findViewById(R.id.checked_app_list_layout);

        Context context = getActivity();
        List<LaunchItem> items = LaunchItemModel.getItemList(context);
        if (items.isEmpty()) {
            mLayout.setVisibility(View.GONE);
        } else {
            for (LaunchItem item : items) {
                View itemView = createAppIcon(context, inflater, item, mLayout);
                itemView.setTag(item);
                itemView.setOnClickListener(this);
                mLayout.addView(itemView);
            }
        }

        LaunchItemModel.getInstance().addEventListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LaunchItemModel.getInstance().removeEventListener(this);
    }

    @Override
    public void onAdded(@NonNull LaunchItem newItem) {
        Log.d("[onAdded] %s", newItem.getId());
        Context context = getActivity();
        if (context == null) {
            Log.e("context is null.");
            return;
        }

        if (mLayout == null) {
            Log.e("layout is null.");
            return;
        }

        View itemView = createAppIcon(context, LayoutInflater.from(context), newItem, mLayout);
        itemView.setTag(newItem);
        itemView.setOnClickListener(this);
        mLayout.addView(itemView);

        if (mLayout.getChildCount() == 1) {
            mLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRemoved(@NonNull String id) {
        Log.d("[onRemoved] %s", id);
        Context context = getActivity();
        if (context == null) {
            Log.e("context is null.");
            return;
        }

        if (mLayout == null) {
            Log.e("layout is null.");
            return;
        }

        int count = mLayout.getChildCount();
        for (int i = 0; i < count; ++i) {
            View child = mLayout.getChildAt(i);
            LaunchItem item = (LaunchItem) child.getTag();
            if (item != null && item.getId().equals(id)) {
                Log.d("[%s] found.", id);
                mLayout.removeView(child);

                if (mLayout.getChildCount() == 0) {
                    mLayout.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        LaunchItem item = (LaunchItem) v.getTag();
        if (item == null) {
            Log.e("item is null.");
            return;
        }

        LaunchItemModel.getInstance().removeItem(v.getContext(), item.getId());
    }
}
