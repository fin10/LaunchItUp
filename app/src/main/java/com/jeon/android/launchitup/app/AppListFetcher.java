package com.jeon.android.launchitup.app;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LauncherItemFetcherCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListFetcher {

    public static void fetch(@NonNull PackageManager packageManager, @NonNull final LauncherItemFetcherCallback callback) {

        new AsyncTask<PackageManager, Void, List<LaunchItem>>() {

            @Override
            protected List<LaunchItem> doInBackground(PackageManager... params) {
                PackageManager pkgManager = params[0];

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> infoList = pkgManager.queryIntentActivities(intent, 0);
                if (infoList == null || infoList.isEmpty()) {
                    Log.e("There is no applications which is able to launch.");
                    return Collections.emptyList();
                }

                List<LaunchItem> launchItemList = new ArrayList<>(infoList.size());
                for (ResolveInfo info : infoList) {
                    try {
                        String pkgName = info.activityInfo.packageName;
                        CharSequence name = info.loadLabel(pkgManager);
                        if (TextUtils.isEmpty(name)) continue;

                        Resources res = pkgManager.getResourcesForApplication(pkgName);
                        int iconResId = info.getIconResource();
                        Uri iconUri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                                .authority(pkgName)
                                .appendPath(res.getResourceTypeName(iconResId))
                                .appendPath(res.getResourceEntryName(iconResId))
                                .build();

                        String uriString = Intent.makeRestartActivityTask(new ComponentName(pkgName, info.activityInfo.name)).toUri(0);

                        LaunchItem data = new LaunchItem.Builder()
                                .setId(uriString)
                                .setTitle((String) name)
                                .setIconUri(iconUri)
                                .setLaunchUri(uriString)
                                .build();

                        if (!launchItemList.contains(data)) {
                            launchItemList.add(data);
                        }
                    } catch (IllegalArgumentException | PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(launchItemList, new Comparator<LaunchItem>() {

                    @Override
                    public int compare(LaunchItem lhs, LaunchItem rhs) {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }
                });

                return launchItemList;
            }

            @Override
            protected void onPostExecute(List<LaunchItem> launchItemList) {
                callback.onResult(launchItemList);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packageManager);
    }
}
