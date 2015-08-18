package com.jeon.android.launchitup;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jeon.android.launchitup.app.AppGridFragment;
import com.jeon.android.launchitup.contact.ContactListFragment;
import com.jeon.android.launchitup.data.LaunchItem;

import java.util.ArrayList;
import java.util.List;

public class LaunchItemDialogActivity extends Activity implements View.OnClickListener {

    private ViewGroup mCheckedLaunchItemListLayout;
    private Toast mUpToFiveToast;

    private static View createAppIcon(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull LaunchItem data, @NonNull ViewGroup parent) {
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
        setContentView(R.layout.activity_launch_item_dialog);

        mCheckedLaunchItemListLayout = (ViewGroup) findViewById(R.id.checked_app_list_layout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new LaunchItemAdapter(getFragmentManager(), getResources()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.setTabTextColors(0x4cffffff, Color.WHITE);
        tabLayout.setupWithViewPager(viewPager);

        View okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_button:
                finish();
                break;
            case R.id.app_icon_image_view: {
                mCheckedLaunchItemListLayout.removeView(v);

                if (mCheckedLaunchItemListLayout.getChildCount() == 0) {
                    mCheckedLaunchItemListLayout.setVisibility(View.GONE);
                }

//        int position = (int) v.getTag();
//        mAppListAdapter.uncheckItem(position);
//        mAppListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public static class LaunchItemAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;
        private final List<String> mTitleList;

        public LaunchItemAdapter(@NonNull FragmentManager fm, @NonNull Resources res) {
            super(fm);
            mTitleList = new ArrayList<>(PAGE_COUNT);
            mTitleList.add(res.getString(R.string.apps));
            mTitleList.add(res.getString(R.string.contacts));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AppGridFragment();
                case 1:
                    return new ContactListFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
