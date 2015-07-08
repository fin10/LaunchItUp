package com.jeon.android.launchitup.data;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListFetcher {

    public static void fetch(@NonNull PackageManager packageManager, @NonNull final Callback callback) {

        new AsyncTask<PackageManager, Void, List<AppData>>() {

            @Override
            protected List<AppData> doInBackground(PackageManager... params) {
                PackageManager pkgManager = params[0];

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> infoList = pkgManager.queryIntentActivities(intent, 0);
                if (infoList == null || infoList.isEmpty()) {
                    Log.e("There is no applications which is able to launch.");
                    return null;
                }

                List<AppData> appDataList = new ArrayList<AppData>(infoList.size());
                for (ResolveInfo info : infoList) {
                    String pkgName = info.activityInfo.applicationInfo.packageName;
                    try {
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

                        String uriString = pkgManager.getLaunchIntentForPackage(pkgName).toUri(0);

                        AppData data = new AppData.Builder()
                                .setId(uriString)
                                .setTitle((String) name)
                                .setIconUri(iconUri)
                                .setLaunchUri(uriString)
                                .build();

                        if (!appDataList.contains(data)) {
                            appDataList.add(data);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(appDataList, new Comparator<AppData>() {

                    @Override
                    public int compare(AppData lhs, AppData rhs) {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }
                });

                return appDataList;
            }

            @Override
            protected void onPostExecute(List<AppData> appDataList) {
                callback.onResult(appDataList);
            }

        }.execute(packageManager);
    }

    public interface Callback {
        void onResult(List<AppData> appDataList);
    }
}
