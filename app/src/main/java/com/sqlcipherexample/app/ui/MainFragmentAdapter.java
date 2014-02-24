package com.sqlcipherexample.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;

public class MainFragmentAdapter extends FragmentPagerAdapter {

    public static final String PAGE_FRAGMENT_TITLE = "title_of_a_fragment";

    private final Fragment[] mFragmentsArray;

    public MainFragmentAdapter(FragmentManager fm, Fragment... fragments) {
        super(fm);
        Fragment[] fragmentsArray = fragments;
        if(fragmentsArray == null) {
            fragmentsArray = new Fragment[1];
            fragmentsArray[0] = BlankFragment.newInstance();
        }

        mFragmentsArray = fragmentsArray;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentsArray[position];
    }

    @Override
    public int getCount() {
        return mFragmentsArray.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = mFragmentsArray[position].getArguments().getString(PAGE_FRAGMENT_TITLE);

        return TextUtils.isEmpty(title) ? "Title" : title;
    }

    private static class BlankFragment extends Fragment {

        static BlankFragment newInstance() {
            BlankFragment fragment = new BlankFragment();
            Bundle args = new Bundle();
            args.putString(MainFragmentAdapter.PAGE_FRAGMENT_TITLE, BlankFragment.class.getSimpleName().toUpperCase());
            return fragment;
        }
    }
}
