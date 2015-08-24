package com.jeon.android.launchitup.contact;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.R;
import com.jeon.android.launchitup.data.CheckableListAdapter;
import com.jeon.android.launchitup.data.LaunchItem;
import com.jeon.android.launchitup.data.LaunchItemModel;
import com.jeon.android.launchitup.data.LauncherItemFetcherCallback;

import java.util.List;

public class ContactListFragment extends Fragment implements LauncherItemFetcherCallback, AdapterView.OnItemClickListener, LaunchItemModel.EventListener {

    private ListView mListView;
    private View mProgressBar;
    private View mEmptyView;
    private ContactListAdapter mAdapter;

    private Toast mUpToFiveToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView");
        ContactListFetcher.fetch(getActivity(), this);

        View root = inflater.inflate(R.layout.fragment_contact_list, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mEmptyView = root.findViewById(R.id.empty_view);
        mProgressBar = root.findViewById(R.id.progress_bar);

        mUpToFiveToast = Toast.makeText(getActivity(), R.string.it_supports_up_to_five_applications, Toast.LENGTH_SHORT);

        return root;
    }

    @Override
    public void onResult(@NonNull List<LaunchItem> launchItemList) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.e("activity is null.");
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mListView.setEmptyView(mEmptyView);

        LayoutInflater inflater = activity.getLayoutInflater();
        mAdapter = new ContactListAdapter(inflater, launchItemList);

        List<LaunchItem> checkedItems = LaunchItemModel.getItemList(activity);
        for (LaunchItem item : launchItemList) {
            if (checkedItems.contains(item)) {
                mAdapter.checkItem(item);
            }
        }

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        LaunchItemModel.getInstance().addEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LaunchItemModel.getInstance().removeEventListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LaunchItem data = (LaunchItem) mAdapter.getItem(position);
        if (data == null) {
            Log.e("[%d] launch item is null.");
            return;
        }

        if (mAdapter.isChecked(data)) {
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
        if (mAdapter != null && !mAdapter.isChecked(newItem)) {
            int count = mAdapter.getCount();
            for (int i = 0; i < count; ++i) {
                LaunchItem item = (LaunchItem) mAdapter.getItem(i);
                if (item.equals(newItem)) {
                    mAdapter.checkItem(item);
                    break;
                }
            }
        }
    }

    @Override
    public void onRemoved(@NonNull String id) {
        Log.d("[onRemoved] %s", id);
        if (mAdapter != null) {
            LaunchItem item = mAdapter.getCheckedItemById(id);
            if (item != null) {
                mAdapter.uncheckItem(item);
            }
        }
    }

    private static class ContactListAdapter extends CheckableListAdapter {

        private final List<LaunchItem> mItems;
        private final LayoutInflater mInflater;

        public ContactListAdapter(LayoutInflater inflater, List<LaunchItem> items) {
            mInflater = inflater;
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.contact_list_item_layout, parent, false);
                convertView.setTag(R.id.photo_view, convertView.findViewById(R.id.photo_view));
                convertView.setTag(R.id.display_name_view, convertView.findViewById(R.id.display_name_view));
                convertView.setTag(R.id.number_view, convertView.findViewById(R.id.number_view));
                convertView.setTag(R.id.check_image_view, convertView.findViewById(R.id.check_image_view));
            }

            LaunchItem item = (LaunchItem) getItem(position);

            TextView textView = (TextView) convertView.getTag(R.id.display_name_view);
            textView.setText(item.getTitle());

            textView = (TextView) convertView.getTag(R.id.number_view);
            textView.setText(item.getSubTitle());

            Glide.with(mInflater.getContext())
                    .load(item.getIconUri())
                    .into((ImageView) convertView.getTag(R.id.photo_view));

            View checkView = (View) convertView.getTag(R.id.check_image_view);
            checkView.setVisibility(mCheckedItems.contains(item) ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }
}
