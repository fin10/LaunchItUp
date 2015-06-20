package com.jeon.android.launchitup.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class AppData implements Comparable<AppData> {

    private String packageName;
    private String name;
    private Drawable drawable;
    private String intent;

    private AppData() {
    }

    public String getName() {
        return name;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public String getIntent() {
        return intent;
    }

    @Override
    public int compareTo(@NonNull AppData another) {
        return name.compareToIgnoreCase(another.name);
    }

    public static class Builder {

        private final AppData data;

        public Builder() {
            data = new AppData();
        }

        public Builder setPackageName(String pkgName) {
            data.packageName = pkgName;
            return this;
        }

        public Builder setAppName(String name) {
            data.name = name;
            return this;
        }

        public Builder setIconDrawable(Drawable drawable) {
            data.drawable = drawable;
            return this;
        }

        public Builder setLaunchIntent(Intent intent) {
            try {
                data.intent = intent.toUri(0);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            return this;
        }

        public AppData build() {
            if (TextUtils.isEmpty(data.packageName))
                throw new IllegalArgumentException("packageName is empty.");
            if (TextUtils.isEmpty(data.name)) throw new IllegalArgumentException("name is empty.");
            if (data.drawable == null) throw new IllegalArgumentException("drawable is null.");
            if (TextUtils.isEmpty(data.intent))
                throw new IllegalArgumentException("intent is null.");

            return data;
        }
    }
}
