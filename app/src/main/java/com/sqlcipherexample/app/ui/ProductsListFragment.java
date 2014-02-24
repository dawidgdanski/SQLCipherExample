package com.sqlcipherexample.app.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.sqlcipherexample.app.Cursors;
import com.sqlcipherexample.app.R;
import com.sqlcipherexample.app.Utils;
import com.sqlcipherexample.app.model.Product;
import com.sqlcipherexample.app.model.contract.ProductsContract;


public class ProductsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static ProductsListFragment newInstance() {
        ProductsListFragment fragment = new ProductsListFragment();
        Bundle args = new Bundle();
        args.putString(MainFragmentAdapter.PAGE_FRAGMENT_TITLE, "Products List".toUpperCase());
        fragment.setArguments(args);

        return fragment;
    }

    private View mHeaderView;
    private SimpleCursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            mHeaderView = inflater.inflate(R.layout.add_product, null);
            final EditText productName = (EditText) mHeaderView.findViewById(R.id.product_title);
            mHeaderView.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = productName.getText().toString();
                    if(! TextUtils.isEmpty(name)) {
                        addProduct(new Product(0, name, 1));
                        productName.setText("");
                    }
                }
            });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ProductsContract.CONTENT_URI,
                null,
                null,
                null,
                ProductsContract.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ListView listView = getListView();
            if(listView.getHeaderViewsCount() == 0) {
                listView.addHeaderView(mHeaderView);
            }

            SimpleCursorAdapter adapter = getAdapter();
            if(adapter != null) {
                adapter.swapCursor(Cursors.returnSameOrEmptyCursor(data));
            } else {
                loadProductsAdapter(Cursors.returnSameOrEmptyCursor(data));
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getAdapter().swapCursor(null);
        setListAdapter(null);
    }

    private void loadProductsAdapter(final Cursor data) {

        SimpleCursorAdapter adapter;

        if(Utils.isAPIVersionEqualOrHigherThan(Build.VERSION_CODES.HONEYCOMB)) {
            adapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1,
                    data,
                    new String[] {
                            ProductsContract.Table.NAME
                    },
                    new int[] {
                            android.R.id.text1
                    },
                    0);
        } else {
            adapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1,
                    data,
                    new String[] {
                            ProductsContract.Table.NAME
                    },
                    new int[] {
                            android.R.id.text1
                    });
        }

        setListAdapter(adapter);
    }

    private void addProduct(final Product product) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri insertionUri = contentResolver.insert(ProductsContract.CONTENT_URI, product.toContentValues());
        contentResolver.notifyChange(insertionUri, null);
        getAdapter().notifyDataSetChanged();
    }

    private SimpleCursorAdapter getAdapter() {
        if(mAdapter == null) {
            mAdapter = ((SimpleCursorAdapter) getListAdapter());
        }

        return mAdapter;
    }
}
