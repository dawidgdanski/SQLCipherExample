package com.sqlcipherexample.app;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.sqlcipherexample.app.database.DatabaseHelper;
import com.sqlcipherexample.app.model.Product;
import com.sqlcipherexample.app.model.contract.ProductsContract;
import com.sqlcipherexample.app.preferences.MySecurePreferences;
import com.sqlcipherexample.app.preferences.SettingsActivity;
import com.sqlcipherexample.app.provider.MyContentProvider;

import net.sqlcipher.database.SQLiteDatabase;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;

    private final int ACTIVITY_RESULT_SETTINGS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            onFirstLaunch();
        }
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), ACTIVITY_RESULT_SETTINGS_REQUEST_CODE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO: Still bug here. SQLException thrown after database access passphrase change.

        if(resultCode == RESULT_OK) {
            if(requestCode == ACTIVITY_RESULT_SETTINGS_REQUEST_CODE && data.hasExtra(DatabaseHelper.DATABASE_MODIFIED)) {
                mAdapter.getCursor().close();
                mAdapter = null;
                getSupportLoaderManager().restartLoader(0, null, this);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                ProductsContract.CONTENT_URI,
                                null,
                                null,
                                null,
                                ProductsContract.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        initAdapter(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.getCursor().close();
        mAdapter = null;
    }

    private void onFirstLaunch() {
        SQLiteDatabase.loadLibs(this);
        setDatabaseKeyIfNotSet();
    }

    private void addProduct(final Product product) {
        ContentResolver contentResolver = getContentResolver();
        Uri insertionUri = contentResolver.insert(ProductsContract.CONTENT_URI, product.toContentValues());
        contentResolver.notifyChange(insertionUri, null);
        mAdapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setDatabaseKeyIfNotSet() {
        MySecurePreferences preferences = MySecurePreferences.getInstance(this);
        if(! preferences.containsDatabaseAccessKey()) {
            final String firstKey = "YOLO";
            MySecurePreferences.getInstance(this).setDatabaseAccessKey(firstKey);
        }

        getContentResolver().call(ProductsContract.CONTENT_URI,
                                  MyContentProvider.SET_KEY_METHOD,
                                  preferences.getDatabaseAccessKey(),
                                  Bundle.EMPTY);
    }

    private void initAdapter(final Cursor data) {

        if(Utils.isAPIVersionEqualOrHigherThan(Build.VERSION_CODES.HONEYCOMB)) {
            mAdapter = new SimpleCursorAdapter(this,
                                               android.R.layout.simple_list_item_1,
                                               Cursors.returnSameOrEmptyCursor(data),
                                               new String[] {
                                                ProductsContract.Table.NAME
                                               },
                                               new int[] {
                                                 android.R.id.text1
                                               },
                                               0);
        } else {
            mAdapter = new SimpleCursorAdapter(this,
                                               android.R.layout.simple_list_item_1,
                                               Cursors.returnSameOrEmptyCursor(data),
                                               new String[] {
                                                ProductsContract.Table.NAME
                                               },
                                               new int[] {
                                                android.R.id.text1
                                               });
        }
        ListView listView = getListView();
        if(listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(prepareHeaderView());
        }
        listView.setAdapter(mAdapter);
    }

    private ListView getListView() {
        return (ListView) findViewById(R.id.list);
    }

    private View prepareHeaderView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_product, null);
        final EditText productName = (EditText) view.findViewById(R.id.product_title);
        view.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = productName.getText().toString();
                if(! TextUtils.isEmpty(name)) {
                    addProduct(new Product(0, name, 1));
                    productName.setText("");
                }
            }
        });

        return view;
    }
}
