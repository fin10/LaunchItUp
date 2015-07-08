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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.data.AppData;
import com.jeon.android.launchitup.data.AppListFetcher;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppChooseDialogActivity extends Activity implements DialogInterface.OnDismissListener, AdapterView.OnItemClickListener, AppListFetcher.Callback, View.OnClickListener {

    private AlertDialog mDialog;
    private AppListAdapter mAppListAdapter;
    private GridView mGridView;
    private ViewGroup mCheckedAppListLayout;
    private View mProgressBar;

    private Toast mUpToFiveToast;

    private static View createAppIcon(Context context, LayoutInflater inflater, AppData data, ViewGroup parent) {
        View layout = inflater.inflate(R.layout.checked_app_item_layout, parent, false);
        ImageView appIcon = (ImageView) layout.findViewById(R.id.app_icon_image_view);
        Glide.with(context)
                .load(data.getIconUri())
                .error(android.R.drawable.ic_menu_help)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(appIcon);

        return layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("enter");
        super.onCreate(savedInstanceState);
        AppListFetcher.fetch(getPackageManager(), this);

        View dialogView = getLayoutInflater().inflate(R.layout.app_list_layout, null);
        mGridView = (GridView) dialogView.findViewById(R.id.grid_view);
        mCheckedAppListLayout = (ViewGroup) dialogView.findViewById(R.id.checked_app_list_layout);
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
                        mAppListAdapter.checkItem(i);

                        View view = createAppIcon(this, inflater, appData, mCheckedAppListLayout);
                        view.setTag(i);
                        view.setOnClickListener(this);
                        mCheckedAppListLayout.addView(view);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (mCheckedAppListLayout.getChildCount() > 0) {
            mCheckedAppListLayout.setVisibility(View.VISIBLE);
        }

        mGridView.setAdapter(mAppListAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mCheckedAppListLayout.removeView(v);

        if (mCheckedAppListLayout.getChildCount() == 0) {
            mCheckedAppListLayout.setVisibility(View.GONE);
        }

        int position = (int) v.getTag();
        mAppListAdapter.uncheckItem(position);
        mAppListAdapter.notifyDataSetChanged();
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
            mAppListAdapter.uncheckItem(position);

            int count = mCheckedAppListLayout.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = mCheckedAppListLayout.getChildAt(i);
                if ((int) child.getTag() == position) {
                    Log.d("[%d] found.", i);
                    mCheckedAppListLayout.removeViewAt(i);

                    if (mCheckedAppListLayout.getChildCount() == 0) {
                        mCheckedAppListLayout.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        } else {
            boolean result = mAppListAdapter.checkItem(position);
            if (result) {
                if (mCheckedAppListLayout.getChildCount() == 0) {
                    mCheckedAppListLayout.setVisibility(View.VISIBLE);
                }

                View appIconView = createAppIcon(this, getLayoutInflater(), data, mCheckedAppListLayout);
                appIconView.setTag(position);
                appIconView.setOnClickListener(this);
                mCheckedAppListLayout.addView(appIconView);
            } else {
                if (mUpToFiveToast == null) {
                    mUpToFiveToast = Toast.makeText(this, R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);
                }

                mUpToFiveToast.show();
            }
        }

        mAppListAdapter.notifyDataSetChanged();
    }

    private static class AppListAdapter extends BaseAdapter {

        private final RequestManager mRequestManager;
        private final LayoutInflater mInflater;
        private final List<AppData> mAppList;

        private final Set<Integer> mSelectedItems;

        public AppListAdapter(@NonNull LayoutInflater inflater, List<AppData> list) {
            mRequestManager = Glide.with(inflater.getContext());
            mInflater = inflater;
            mAppList = list;
            mSelectedItems = new HashSet<Integer>();
        }

        public boolean checkItem(int position) {
            if (mSelectedItems.size() >= 5) {
                Log.d("max:%d", mSelectedItems.size());
                return false;
            }

            if (mSelectedItems.contains(position)) {
                Log.e("[%s] already selected.", position);
            } else {
                mSelectedItems.add(position);
            }

            return true;
        }

        public void uncheckItem(int position) {
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
            mRequestManager
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
