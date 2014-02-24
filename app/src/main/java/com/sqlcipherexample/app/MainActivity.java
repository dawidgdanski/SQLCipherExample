package com.sqlcipherexample.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sqlcipherexample.app.model.contract.ProductsContract;
import com.sqlcipherexample.app.preferences.MySecurePreferences;
import com.sqlcipherexample.app.preferences.SettingsActivity;
import com.sqlcipherexample.app.provider.MyContentProvider;
import com.sqlcipherexample.app.ui.MainFragmentAdapter;
import com.sqlcipherexample.app.ui.ProductsListFragment;

public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

    private final String CURRENT_FRAGMENT = "current fragment";

    private int mCurrentFragment = 0;

    MainFragmentAdapter mFragmentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDatabaseKeyIfNotSet();
        initializeViewPager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(CURRENT_FRAGMENT, mCurrentFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentFragment = savedInstanceState.getInt(CURRENT_FRAGMENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViewPager() {
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(createAdapter());
        pager.setOnPageChangeListener(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setDatabaseKeyIfNotSet() {
        MySecurePreferences preferences = MySecurePreferences.getInstance(this);
        if(! preferences.isContainsDatabaseAccessKey()) {
            final String firstKey = "YOLO";
            MySecurePreferences.getInstance(this).setDatabaseAccessKey(firstKey);
        }

        getContentResolver().call(ProductsContract.CONTENT_URI,
                                  MyContentProvider.SET_KEY_METHOD,
                                  preferences.getDatabaseAccessKey(),
                                  Bundle.EMPTY);
    }

    private FragmentPagerAdapter createAdapter() {
        if(mFragmentsAdapter == null) {
            mFragmentsAdapter=  new MainFragmentAdapter(getSupportFragmentManager(),
                                                        ProductsListFragment.newInstance());
        }

        return mFragmentsAdapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentFragment = position;
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentFragment = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
