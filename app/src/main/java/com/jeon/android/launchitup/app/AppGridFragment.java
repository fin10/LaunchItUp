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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.R;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LaunchItemModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppGridFragment extends Fragment implements AppListFetcher.Callback, AdapterView.OnItemClickListener, LaunchItemModel.EventListener {

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

//                        View view = createAppIcon(this, inflater, launchItem, mCheckedAppListLayout);
//                        view.setTag(i);
//                        view.setOnClickListener(this);
//                        mCheckedAppListLayout.addView(view);
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
            boolean result = LaunchItemModel.getInstance().removeItem(getActivity(), data.getId());
            if (result) {
                mAppListAdapter.uncheckItem(data);
            }

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
            boolean result = LaunchItemModel.getInstance().putItem(getActivity(), data);
            if (result) {
                mAppListAdapter.checkItem(data);
            } else {
                mUpToFiveToast.show();
            }
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

    @Override
    public void onAdded(@NonNull LaunchItem newItem) {
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
        if (mAppListAdapter != null) {
            LaunchItem item = mAppListAdapter.getCheckedItemById(id);
            if (item != null) {
                mAppListAdapter.uncheckItem(item);
            }
        }
    }

    private static class AppListAdapter extends BaseAdapter {

        private final RequestManager mRequestManager;
        private final LayoutInflater mInflater;
        private final List<LaunchItem> mAppList;
        private final Set<LaunchItem> mCheckedItems;

        public AppListAdapter(@NonNull LayoutInflater inflater, @NonNull List<LaunchItem> list) {
            mRequestManager = Glide.with(inflater.getContext());
            mInflater = inflater;
            mAppList = list;
            mCheckedItems = new HashSet<>();
        }

        public void checkItem(@NonNull LaunchItem item) {
            if (mCheckedItems.contains(item)) {
                Log.e("[%s] already selected.", item.getId());
            } else {
                mCheckedItems.add(item);
            }
        }

        public void uncheckItem(@NonNull LaunchItem item) {
            mCheckedItems.remove(item);
        }

        public boolean isChecked(@NonNull LaunchItem item) {
            return mCheckedItems.contains(item);
        }

        @Nullable
        public LaunchItem getCheckedItemById(@NonNull String id) {
            for (LaunchItem item : mCheckedItems) {
                if (item.getId().equals(id)) {
                    return item;
                }
            }

            return null;
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
