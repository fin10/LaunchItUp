package com.jeon.android.launchitup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeon.android.launchitup.data.AppData;
import com.jeon.android.launchitup.data.AppListFetcher;

import java.util.List;

public class AppChooseDialogActivity extends Activity implements DialogInterface.OnDismissListener, AdapterView.OnItemClickListener, AppListFetcher.Callback {

    private AlertDialog mDialog;
    private AppListAdapter mAppListAdapter;
    private GridView mGridView;
    private View mProgressBar;
    private Toast mGuideToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("enter");
        super.onCreate(savedInstanceState);

        mGuideToast = Toast.makeText(this, R.string.long_press_the_home_key, Toast.LENGTH_LONG);

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
        Survey.send(Survey.Action.ENTER, "AppChooseDialog");
    }

    @Override
    protected void onDestroy() {
        Log.d("enter");
        super.onDestroy();
        if (mDialog != null) mDialog.dismiss();
    }

    @Override
    public void onResult(List<AppData> appDataList) {
        mProgressBar.setVisibility(View.GONE);

        LayoutInflater inflater = getLayoutInflater();
        mAppListAdapter = new AppListAdapter(inflater, appDataList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uriString = prefs.getString(LauncherActivity.PREF_KEY_LAUNCH_INTENT, null);
        mAppListAdapter.selectItem(uriString);

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(LauncherActivity.PREF_KEY_LAUNCH_INTENT, data.getIntent()).apply();

        mAppListAdapter.selectItem(data.getIntent());
        mAppListAdapter.notifyDataSetChanged();

        mGuideToast.show();
        Survey.send(Survey.Action.CLICK, "select application");
    }

    private static class AppListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;

        private String mSelectedItem;
        private List<AppData> mAppList;

        public AppListAdapter(LayoutInflater inflater, List<AppData> list) {
            mInflater = inflater;
            mAppList = list;
        }

        public void selectItem(String uriString) {
            mSelectedItem = uriString;
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
            nameView.setText(appData.getName());

            ImageView iconView = (ImageView) convertView.getTag(R.id.app_icon_image_view);
            iconView.setImageDrawable(appData.getDrawable());

            View checkView = (View) convertView.getTag(R.id.check_image_view);
            checkView.setVisibility(appData.getIntent().equalsIgnoreCase(mSelectedItem) ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }
}
