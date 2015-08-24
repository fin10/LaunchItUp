package com.jeon.android.launchitup.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jeon.android.launchitup.BuildConfig;
import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LauncherItemFetcherCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactListFetcher {

    private static final Uri EMPTY_PHOTO_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath("mipmap")
            .appendPath("ic_person_black_48dp")
            .build();

    public static void fetch(@NonNull final Context context, @NonNull final LauncherItemFetcherCallback callback) {

        new AsyncTask<Void, Void, List<LaunchItem>>() {

            @Override
            protected List<LaunchItem> doInBackground(Void... params) {

                String[] proj = {
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                };
                Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null, null);
                if (cursor == null) {
                    return Collections.emptyList();
                }

                try {
                    List<LaunchItem> items = new ArrayList<>();
                    Map<String, LaunchItem> itemMap = new HashMap<>();

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID));
                        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String photoThumbUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

                        if (TextUtils.isEmpty(displayName)) {
                            Log.e("[%d] display is empty.", id);
                            continue;
                        }

                        if (TextUtils.isEmpty(number)) {
                            Log.e("[%d] number is empty.", id);
                            continue;
                        }

                        Uri thumbnailUri = EMPTY_PHOTO_URI;
                        if (!TextUtils.isEmpty(photoThumbUri)) {
                            thumbnailUri = Uri.parse(photoThumbUri);
                        }

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + number));

                        try {
                            LaunchItem item = new LaunchItem.Builder()
                                    .setId(String.valueOf(id))
                                    .setTitle(displayName)
                                    .setSubTitle(number)
                                    .setLaunchUri(intent.toUri(0))
                                    .setIconUri(thumbnailUri)
                                    .build();

                            if (!itemMap.containsKey(number)) {
                                itemMap.put(number, item);
                                items.add(item);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }

                    Collections.sort(items, new Comparator<LaunchItem>() {

                        @Override
                        public int compare(LaunchItem lhs, LaunchItem rhs) {
                            return lhs.getTitle().compareTo(rhs.getTitle());
                        }
                    });

                    return items;

                } finally {
                    cursor.close();
                }
            }

            @Override
            protected void onPostExecute(List<LaunchItem> launchItemList) {
                callback.onResult(launchItemList);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
