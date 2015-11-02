package com.jeon.android.launchitup.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class LaunchItem {

    private String mId;
    private String mTitle;
    private String mSubTitle = "";
    private Uri mIconUri;
    private String mLaunchUriString;
    private long mRegistrationTime = 0;
    private boolean mShowTitle = false;

    private LaunchItem() {
    }

    LaunchItem(@NonNull String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        mId = jsonObject.getString("id");
        mTitle = jsonObject.getString("title");
        if (jsonObject.has("sub_title")) mSubTitle = jsonObject.getString("sub_title");
        mIconUri = Uri.parse(jsonObject.getString("icon_uri"));
        mLaunchUriString = jsonObject.getString("launch_uri_string");
        if (jsonObject.has("registration_time")) mRegistrationTime = jsonObject.getLong("registration_time");
        if (jsonObject.has("show_title")) mShowTitle = jsonObject.getBoolean("show_title");
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public Uri getIconUri() {
        return mIconUri;
    }

    public String getLaunchUriString() {
        return mLaunchUriString;
    }

    public long getRegistrationTime() {
        return mRegistrationTime;
    }

    public void setRegistrationTime(long registrationTime) {
        mRegistrationTime = registrationTime;
    }

    public boolean isShownTitle() {
        return mShowTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LaunchItem launchItem = (LaunchItem) o;

        return !(mId != null ? !mId.equals(launchItem.mId) : launchItem.mId != null);

    }

    @Override
    public int hashCode() {
        return mId != null ? mId.hashCode() : 0;
    }

    @NonNull
    String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", mId);
        json.put("title", mTitle);
        json.put("sub_title", mSubTitle);
        json.put("icon_uri", mIconUri);
        json.put("launch_uri_string", mLaunchUriString);
        json.put("registration_time", mRegistrationTime);
        json.put("show_title", mShowTitle);

        return json.toString();
    }

    public static final class Builder {

        private final LaunchItem data;

        public Builder() {
            data = new LaunchItem();
        }

        @NonNull
        public Builder setId(@NonNull String id) {
            data.mId = id;
            return this;
        }

        @NonNull
        public Builder setTitle(@NonNull String title) {
            data.mTitle = title;
            return this;
        }

        @NonNull
        public Builder setSubTitle(@Nullable String subTitle) {
            data.mSubTitle = subTitle;
            return this;
        }

        @NonNull
        public Builder setIconUri(@NonNull Uri uri) {
            data.mIconUri = uri;
            return this;
        }

        @NonNull
        public Builder setLaunchUri(@NonNull String uriString) {
            data.mLaunchUriString = uriString;
            return this;
        }

        @NonNull
        public Builder setShowTitle(boolean show) {
            data.mShowTitle = show;
            return this;
        }

        @NonNull
        public LaunchItem build() {
            if (TextUtils.isEmpty(data.mId))
                throw new IllegalArgumentException("id is empty.");
            if (TextUtils.isEmpty(data.mTitle))
                throw new IllegalArgumentException("title is empty.");
            if (data.mIconUri == null) throw new IllegalArgumentException("icon uri is null.");
            if (TextUtils.isEmpty(data.mLaunchUriString))
                throw new IllegalArgumentException("launch uri is null.");

            return data;
        }
    }
}
