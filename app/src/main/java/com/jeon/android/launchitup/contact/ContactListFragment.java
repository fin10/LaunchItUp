package com.jeon.android.launchitup.contact;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeon.android.launchitup.Log;
import com.jeon.android.launchitup.R;

public class ContactListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView");
        View root = inflater.inflate(R.layout.fragment_contact_list, container, false);

        return root;
    }
}
