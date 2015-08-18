package com.jeon.android.launchitup.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.LauncherActivity;
import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.R;
import com.jeon.android.launchitup.data.LaunchItem;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppGridFragment extends Fragment implements AppListFetcher.Callback, AdapterView.OnItemClickListener {

    private GridView mGridView;
    private View mProgressBar;
    private View mEmptyView;
    private AppListAdapter mAppListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView");
        AppListFetcher.fetch(getActivity().getPackageManager(), this);

        View root = inflater.inflate(R.layout.fragment_app_grid, container, false);
        mGridView = (GridView) root.findViewById(R.id.grid_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAppListAdapter != null) {
            Set<Integer> selectedItems = mAppListAdapter.getCheckedItems();
            Set<String> datas = new HashSet<>();
            for (int index : selectedItems) {
                LaunchItem data = (LaunchItem) mAppListAdapter.getItem(index);
                datas.add(data.toJson());
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().putStringSet(LauncherActivity.PREF_KEY_LAUNCH_DATA_LIST, datas).apply();
        }
    }

    @Override
    public void onResult(@NonNull List<LaunchItem> launchItemList) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.e("activity is null.");
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mGridView.setEmptyView(mEmptyView);

        LayoutInflater inflater = activity.getLayoutInflater();
        mAppListAdapter = new AppListAdapter(inflater, launchItemList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> dataList = prefs.getStringSet(LauncherActivity.PREF_KEY_LAUNCH_DATA_LIST, Collections.<String>emptySet());
        for (String data : dataList) {
            try {
                LaunchItem launchItem = new LaunchItem(data);
                for (int i = 0; i < launchItemList.size(); ++i) {
                    if (launchItem.equals(launchItemList.get(i))) {
                        mAppListAdapter.checkItem(i);

//                        View view = createAppIcon(this, inflater, launchItem, mCheckedAppListLayout);
//                        view.setTag(i);
//                        view.setOnClickListener(this);
//                        mCheckedAppListLayout.addView(view);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LaunchItem data = (LaunchItem) mAppListAdapter.getItem(position);
        if (data == null) {
            Log.e("[%d] launch item is null.");
            return;
        }

        if (mAppListAdapter.isChecked(position)) {
            mAppListAdapter.uncheckItem(position);

//            int count = mCheckedAppListLayout.getChildCount();
//            for (int i = 0; i < count; ++i) {
//                View child = mCheckedAppListLayout.getChildAt(i);
//                if ((int) child.getTag() == position) {
//                    Log.d("[%d] found.", i);
//                    mCheckedAppListLayout.removeViewAt(i);
//
//                    if (mCheckedAppListLayout.getChildCount() == 0) {
//                        mCheckedAppListLayout.setVisibility(View.GONE);
//                    }
//                    break;
//                }
//            }
        } else {
            boolean result = mAppListAdapter.checkItem(position);
//            if (result) {
//                if (mCheckedAppListLayout.getChildCount() == 0) {
//                    mCheckedAppListLayout.setVisibility(View.VISIBLE);
//                }
//
//                View appIconView = createAppIcon(this, getLayoutInflater(), data, mCheckedAppListLayout);
//                appIconView.setTag(position);
//                appIconView.setOnClickListener(this);
//                mCheckedAppListLayout.addView(appIconView);
//            } else {
//                if (mUpToFiveToast == null) {
//                    mUpToFiveToast = Toast.makeText(this, R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);
//                }
//
//                mUpToFiveToast.show();
//            }
        }

        mAppListAdapter.notifyDataSetChanged();
    }

    private static class AppListAdapter extends BaseAdapter {

        private final RequestManager mRequestManager;
        private final LayoutInflater mInflater;
        private final List<LaunchItem> mAppList;

        private final Set<Integer> mCheckedItems;

        public AppListAdapter(@NonNull LayoutInflater inflater, @NonNull List<LaunchItem> list) {
            mRequestManager = Glide.with(inflater.getContext());
            mInflater = inflater;
            mAppList = list;
            mCheckedItems = new HashSet<>();
        }

        public boolean checkItem(int position) {
            if (mCheckedItems.size() >= 5) {
                Log.d("max:%d", mCheckedItems.size());
                return false;
            }

            if (mCheckedItems.contains(position)) {
                Log.e("[%s] already selected.", position);
            } else {
                mCheckedItems.add(position);
            }

            return true;
        }

        public void uncheckItem(int position) {
            mCheckedItems.remove(position);
        }

        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_grid_item_layout, parent, false);
                convertView.setTag(R.id.app_icon_image_view, convertView.findViewById(R.id.app_icon_image_view));
                convertView.setTag(R.id.check_image_view, convertView.findViewById(R.id.check_image_view));
                convertView.setTag(R.id.app_name_text_view, convertView.findViewById(R.id.app_name_text_view));
            }

            LaunchItem launchItem = mAppList.get(position);
            TextView nameView = (TextView) convertView.getTag(R.id.app_name_text_view);
            nameView.setText(launchItem.getTitle());

            ImageView iconView = (ImageView) convertView.getTag(R.id.app_icon_image_view);
            mRequestManager
                    .load(launchItem.getIconUri())
                    .error(android.R.drawable.ic_menu_help)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(iconView);

            View checkView = (View) convertView.getTag(R.id.check_image_view);
            checkView.setVisibility(mCheckedItems.contains(position) ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }

        public boolean isChecked(int position) {
            return mCheckedItems.contains(position);
        }

        public Set<Integer> getCheckedItems() {
            return mCheckedItems;
        }
    }
}
