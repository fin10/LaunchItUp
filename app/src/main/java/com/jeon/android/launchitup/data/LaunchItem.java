package com.jeon.android.launchitup.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class LaunchItem {

    private String mId;
    private String mTitle;
    private String mSubTitle = "";
    private Uri mIconUri;
    private String mLaunchUriString;

    private LaunchItem() {
    }

    public LaunchItem(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        mId = jsonObject.getString("id");
        mTitle = jsonObject.getString("title");
        if (jsonObject.has("sub_title")) mSubTitle = jsonObject.getString("sub_title");
        mIconUri = Uri.parse(jsonObject.getString("icon_uri"));
        mLaunchUriString = jsonObject.getString("launch_uri_string");
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

    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", mId);
            json.put("title", mTitle);
            json.put("sub_title", mSubTitle);
            json.put("icon_uri", mIconUri);
            json.put("launch_uri_string", mLaunchUriString);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static class Builder {

        private final LaunchItem data;

        public Builder() {
            data = new LaunchItem();
        }

        public Builder setId(@NonNull String id) {
            data.mId = id;
            return this;
        }

        public Builder setTitle(@NonNull String title) {
            data.mTitle = title;
            return this;
        }

        public Builder setSubTitle(@NonNull String subTitle) {
            data.mSubTitle = subTitle;
            return this;
        }

        public Builder setIconUri(@NonNull Uri uri) {
            data.mIconUri = uri;
            return this;
        }

        public Builder setLaunchUri(@NonNull String uriString) {
            data.mLaunchUriString = uriString;
            return this;
        }

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
