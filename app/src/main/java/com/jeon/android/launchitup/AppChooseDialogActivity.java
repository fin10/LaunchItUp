package com.jeon.android.launchitup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.data.AppData;
import com.jeon.android.launchitup.data.AppListFetcher;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppChooseDialogActivity extends Activity implements DialogInterface.OnDismissListener, AdapterView.OnItemClickListener, AppListFetcher.Callback {

    private AlertDialog mDialog;
    private AppListAdapter mAppListAdapter;
    private GridView mGridView;
    private View mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("enter");
        super.onCreate(savedInstanceState);

        AppListFetcher.fetch(getPackageManager(), this);

        View dialogView = getLayoutInflater().inflate(R.layout.app_list_layout, null);
        mGridView = (GridView) dialogView.findViewById(R.id.grid_view);
        mProgressBar = dialogView.findViewById(R.id.progress_bar);

        mDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setOnDismissListener(this)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        Log.d("enter");
        super.onDestroy();
        if (mDialog != null) mDialog.dismiss();

        if (mAppListAdapter != null) {
            Set<Integer> selectedItems = mAppListAdapter.getSelectedItems();
            Set<String> datas = new HashSet<String>();
            for (int index : selectedItems) {
                AppData data = (AppData) mAppListAdapter.getItem(index);
                datas.add(data.toJson());
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putStringSet(LauncherActivity.PREF_KEY_LAUNCH_DATA_LIST, datas).apply();
        }
    }

    @Override
    public void onResult(List<AppData> appDataList) {
        mProgressBar.setVisibility(View.GONE);

        LayoutInflater inflater = getLayoutInflater();
        mAppListAdapter = new AppListAdapter(inflater, appDataList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> dataList = prefs.getStringSet(LauncherActivity.PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        for (String data : dataList) {
            try {
                AppData appData = new AppData(data);
                for (int i = 0; i < appDataList.size(); ++i) {
                    if (appData.equals(appDataList.get(i))) {
                        mAppListAdapter.selectItem(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mGridView.setAdapter(mAppListAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppData data = (AppData) mAppListAdapter.getItem(position);
        if (data == null) {
            Log.e("[%d] app data is null.");
            return;
        }

        if (mAppListAdapter.isSelected(position)) {
            mAppListAdapter.unselectItem(position);
        } else {
            mAppListAdapter.selectItem(position);
        }

        mAppListAdapter.notifyDataSetChanged();
    }

    private static class AppListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final List<AppData> mAppList;

        private final Set<Integer> mSelectedItems;

        public AppListAdapter(@NonNull LayoutInflater inflater, List<AppData> list) {
            mContext = inflater.getContext();
            mInflater = inflater;
            mAppList = list;
            mSelectedItems = new HashSet<Integer>();
        }

        public void selectItem(int position) {
            if (mSelectedItems.contains(position)) {
                Log.e("[%s] already selected.", position);
            } else {
                mSelectedItems.add(position);
            }
        }

        public void unselectItem(int position) {
            mSelectedItems.remove(position);
        }

        @Override
        public int getCount() {
            return mAppList != null ? mAppList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mAppList != null ? mAppList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_list_item_layout, parent, false);
                convertView.setTag(R.id.app_icon_image_view, convertView.findViewById(R.id.app_icon_image_view));
                convertView.setTag(R.id.check_image_view, convertView.findViewById(R.id.check_image_view));
                convertView.setTag(R.id.app_name_text_view, convertView.findViewById(R.id.app_name_text_view));
            }

            AppData appData = mAppList.get(position);
            TextView nameView = (TextView) convertView.getTag(R.id.app_name_text_view);
            nameView.setText(appData.getTitle());

            ImageView iconView = (ImageView) convertView.getTag(R.id.app_icon_image_view);
            Glide.with(mContext)
                    .load(appData.getIconUri())
                    .error(android.R.drawable.ic_menu_help)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(iconView);

            View checkView = (View) convertView.getTag(R.id.check_image_view);
            checkView.setVisibility(mSelectedItems.contains(position) ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }

        public boolean isSelected(int position) {
            return mSelectedItems.contains(position);
        }

        public Set<Integer> getSelectedItems() {
            return mSelectedItems;
        }
    }
}
