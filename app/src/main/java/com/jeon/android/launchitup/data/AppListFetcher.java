package com.jeon.android.launchitup.data;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFetcher {

    public static void fetch(@NonNull final PackageManager packageManager, @NonNull final Callback callback) {

        new AsyncTask<PackageManager, Void, List<AppData>>() {

            @Override
            protected List<AppData> doInBackground(PackageManager... params) {
                PackageManager pkgManager = params[0];
                long startTime = System.currentTimeMillis();

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
                    if (Log.getPackageName().equalsIgnoreCase(pkgName)) continue;

                    intent = pkgManager.getLaunchIntentForPackage(pkgName);

                    CharSequence name = info.loadLabel(pkgManager);
                    if (TextUtils.isEmpty(name)) continue;

                    Drawable drawable = info.activityInfo.loadIcon(pkgManager);

                    try {
                        AppData data = new AppData.Builder()
                                .setPackageName(pkgName)
                                .setAppName((String) name)
                                .setIconDrawable(drawable)
                                .setLaunchIntent(intent)
                                .build();

                        appDataList.add(data);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(appDataList);

                long diff = (System.currentTimeMillis() - startTime) / 1000;
                Log.d("diff:%d", diff);
                if (diff > 0) Survey.send("load applications", diff);

                return appDataList;
            }

            @Override
            protected void onPostExecute(List<AppData> appDatas) {
                callback.onResult(appDatas);
            }

        }.execute(packageManager);
    }

    public interface Callback {
        void onResult(List<AppData> appDataList);
    }
}
