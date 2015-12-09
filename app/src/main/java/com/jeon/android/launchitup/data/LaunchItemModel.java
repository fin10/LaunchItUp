package com.jeon.android.launchitup.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.jeon.android.launchitup.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class LaunchItemModel {

    private static final String PREF_KEY_LAUNCH_DATA_LIST = "pref_key_launch_data_list";

    private static LaunchItemModel sInstance;

    private final Handler mHandler;
    private final List<EventListener> mListeners;

    private LaunchItemModel() {
        mHandler = new Handler();
        mListeners = new LinkedList<>();
    }

    public static LaunchItemModel getInstance() {
        synchronized (LaunchItemModel.class) {
            if (sInstance == null) {
                sInstance = new LaunchItemModel();
            }

            return sInstance;
        }
    }

    @NonNull
    public static List<LaunchItem> getItemList(@NonNull Context context) {
        List<LaunchItem> items = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        for (String data : dataList) {
            try {
                items.add(new LaunchItem(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(items, new Comparator<LaunchItem>() {

            @Override
            public int compare(LaunchItem lhs, LaunchItem rhs) {
                return Long.compare(lhs.getRegistrationTime(), rhs.getRegistrationTime());
            }
        });

        return items;
    }

    public void addEventListener(@NonNull EventListener listener) {
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    public void removeEventListener(@NonNull EventListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public boolean putItem(@NonNull Context context, @NonNull final LaunchItem data) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
            if (dataList.size() >= 5) {
                return false;
            }

            Set<String> items = new HashSet<>(dataList);
            data.setRegistrationTime(System.currentTimeMillis());
            items.add(data.toJsonString());
            prefs.edit().putStringSet(PREF_KEY_LAUNCH_DATA_LIST, items).apply();

            synchronized (mListeners) {
                for (final EventListener listener : mListeners) {
                    mHandler.post(new ItemAddRunner(listener, data));
                }
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean removeItem(@NonNull Context context, @NonNull final String id) {
        try {
            boolean found = false;
            Set<String> items = new HashSet<>();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> dataList = prefs.getStringSet(PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
            for (String data : dataList) {
                LaunchItem item = new LaunchItem(data);
                if (id.equals(item.getId())) {
                    found = true;
                } else {
                    items.add(data);
                }
            }

            if (!found) {
                Log.e("[%s] not found.", id);
                return false;
            }

            prefs.edit().putStringSet(PREF_KEY_LAUNCH_DATA_LIST, items).apply();

            synchronized (mListeners) {
                for (final EventListener listener : mListeners) {
                    mHandler.post(new ItemRemoveRunner(listener, id));
                }
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public interface EventListener {
        void onAdded(@NonNull LaunchItem item);

        void onRemoved(@NonNull String id);
    }

    private static class ItemAddRunner implements Runnable {

        private final EventListener listener;
        private final LaunchItem data;

        public ItemAddRunner(@NonNull EventListener listener, @NonNull LaunchItem data) {
            this.listener = listener;
            this.data = data;
        }

        @Override
        public void run() {
            listener.onAdded(data);
        }
    }

    private static class ItemRemoveRunner implements Runnable {

        private final EventListener listener;
        private final String id;

        public ItemRemoveRunner(@NonNull EventListener listener, @NonNull String id) {
            this.listener = listener;
            this.id = id;
        }

        @Override
        public void run() {
            listener.onRemoved(id);
        }
    }
}
