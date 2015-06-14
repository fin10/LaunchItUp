package com.jeon.android.launchitup.model;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.Survey;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppModel {

    private final Handler mHandler;
    private final WeakReference<OnAppModelListener> mListener;

    public AppModel(OnAppModelListener listener) {
        mHandler = new WeakRefHandler(this);
        mListener = new WeakReference<OnAppModelListener>(listener);
    }

    public void requestAppList(@NonNull final PackageManager packageManager) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                    List<ResolveInfo> infoList = packageManager.queryIntentActivities(intent, 0);
                    if (infoList == null || infoList.isEmpty()) {
                        Log.e("There is no applications which is able to launch.");
                        mHandler.sendEmptyMessage(0);
                        return;
                    }

                    List<AppData> appDataList = new ArrayList<AppData>(infoList.size());
                    for (ResolveInfo info : infoList) {
                        String pkgName = info.activityInfo.applicationInfo.packageName;
                        if (Log.getPackageName().equalsIgnoreCase(pkgName)) continue;

                        intent = packageManager.getLaunchIntentForPackage(pkgName);

                        CharSequence name = info.loadLabel(packageManager);
                        if (TextUtils.isEmpty(name)) continue;

                        Drawable drawable = info.activityInfo.loadIcon(packageManager);

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

                    Message msg = mHandler.obtainMessage();
                    msg.obj = appDataList;
                    mHandler.sendMessage(msg);

                } catch (Exception e) {
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();
    }

    private static class WeakRefHandler extends Handler {

        private final WeakReference<AppModel> mInstance;

        public WeakRefHandler(AppModel model) {
            mInstance = new WeakReference<AppModel>(model);
        }

        @Override
        public void handleMessage(Message msg) {
            AppModel model = mInstance.get();
            if (model != null) {
                OnAppModelListener listener = model.mListener.get();
                if (listener != null) {
                    listener.onResult((List<AppData>) msg.obj);
                }
            }
        }
    }
}
