package com.jeon.android.launchitup.data;

import android.support.annotation.NonNull;

import java.util.List;

public interface LauncherItemFetcherCallback {
    void onResult(@NonNull List<LaunchItem> launchItemList);
}
