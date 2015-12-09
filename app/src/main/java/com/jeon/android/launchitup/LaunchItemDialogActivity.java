package com.jeon.android.launchitup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jeon.android.launchitup.app.AppGridFragment;
import com.jeon.android.launchitup.contact.ContactListFragment;

import java.util.ArrayList;
import java.util.List;

public final class LaunchItemDialogActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("enter");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_item_dialog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new LaunchItemAdapter(getFragmentManager(), getResources()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
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
        }
    }

    private static class LaunchItemAdapter extends FragmentPagerAdapter {

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
