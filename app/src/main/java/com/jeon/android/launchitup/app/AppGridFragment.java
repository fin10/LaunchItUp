package com.jeon.android.launchitup.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.R;
import com.jeon.android.launchitup.data.CheckableListAdapter;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LaunchItemModel;
import com.jeon.android.launchitup.data.LauncherItemFetcherCallback;

import java.util.List;

public class AppGridFragment extends Fragment implements LauncherItemFetcherCallback, AdapterView.OnItemClickListener, LaunchItemModel.EventListener {

    private GridView mGridView;
    private View mProgressBar;
    private View mEmptyView;
    private AppListAdapter mAppListAdapter;

    private Toast mUpToFiveToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView");
        AppListFetcher.fetch(getActivity().getPackageManager(), this);

        View root = inflater.inflate(R.layout.fragment_app_grid, container, false);
        mGridView = (GridView) root.findViewById(R.id.grid_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);

        mUpToFiveToast = Toast.makeText(getActivity(), R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LaunchItemModel.getInstance().removeEventListener(this);
    }

    @Override
    public void onResult(@NonNull List<LaunchItem> items) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.e("activity is null.");
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mGridView.setEmptyView(mEmptyView);

        LayoutInflater inflater = activity.getLayoutInflater();
        mAppListAdapter = new AppListAdapter(inflater, items);

        List<LaunchItem> checkedItems = LaunchItemModel.getItemList(activity);
        for (LaunchItem item : items) {
            if (checkedItems.contains(item)) {
                mAppListAdapter.checkItem(item);
            }
        }

        mGridView.setAdapter(mAppListAdapter);
        mGridView.setOnItemClickListener(this);
        LaunchItemModel.getInstance().addEventListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LaunchItem data = (LaunchItem) mAppListAdapter.getItem(position);
        if (data == null) {
            Log.e("[%d] launch item is null.");
            return;
        }

        if (mAppListAdapter.isChecked(data)) {
            LaunchItemModel.getInstance().removeItem(getActivity(), data.getId());
        } else {
            boolean result = LaunchItemModel.getInstance().putItem(getActivity(), data);
            if (!result) {
                mUpToFiveToast.show();
            }
        }
    }

    @Override
    public void onAdded(@NonNull LaunchItem newItem) {
        Log.d("[onAdded] %s", newItem.getId());
        if (mAppListAdapter != null && !mAppListAdapter.isChecked(newItem)) {
            int count = mAppListAdapter.getCount();
            for (int i = 0; i < count; ++i) {
                LaunchItem item = (LaunchItem) mAppListAdapter.getItem(i);
                if (item.equals(newItem)) {
                    mAppListAdapter.checkItem(item);
                    break;
                }
            }
        }
    }

    @Override
    public void onRemoved(@NonNull String id) {
        Log.d("[onRemoved] %s", id);
        if (mAppListAdapter != null) {
            LaunchItem item = mAppListAdapter.getCheckedItemById(id);
            if (item != null) {
                mAppListAdapter.uncheckItem(item);
            }
        }
    }

    private static class AppListAdapter extends CheckableListAdapter {

        private final RequestManager mRequestManager;
        private final LayoutInflater mInflater;
        private final List<LaunchItem> mAppList;

        public AppListAdapter(@NonNull LayoutInflater inflater, @NonNull List<LaunchItem> list) {
            mRequestManager = Glide.with(inflater.getContext());
            mInflater = inflater;
            mAppList = list;
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
            checkView.setVisibility(mCheckedItems.contains(launchItem) ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }
}
