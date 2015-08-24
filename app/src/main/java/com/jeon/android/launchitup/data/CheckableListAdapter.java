package com.jeon.android.launchitup.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

import com.jeon.android.launchitup.Log;

import java.util.HashSet;
import java.util.Set;

public abstract class CheckableListAdapter extends BaseAdapter {

    protected final Set<LaunchItem> mCheckedItems = new HashSet<>();

    public void checkItem(@NonNull LaunchItem item) {
        if (mCheckedItems.contains(item)) {
            Log.e("[%s] already selected.", item.getId());
        } else {
            mCheckedItems.add(item);
            notifyDataSetChanged();
        }
    }

    public void uncheckItem(@NonNull LaunchItem item) {
        boolean result = mCheckedItems.remove(item);
        if (result) {
            notifyDataSetChanged();
        }
    }

    public boolean isChecked(@NonNull LaunchItem item) {
        return mCheckedItems.contains(item);
    }

    @Nullable
    public LaunchItem getCheckedItemById(@NonNull String id) {
        for (LaunchItem item : mCheckedItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }
}
