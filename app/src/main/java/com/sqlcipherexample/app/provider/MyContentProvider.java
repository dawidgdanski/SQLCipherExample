package com.sqlcipherexample.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.sqlcipherexample.app.database.DatabaseHelper;
import com.sqlcipherexample.app.model.contract.ProductsContract;
import com.sqlcipherexample.app.preferences.MySecurePreferences;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import java.util.UUID;

public class MyContentProvider extends ContentProvider {

    public static final String SET_KEY_METHOD = "setKey";

    private static final UriMatcher sURIMatcher;

    private static final int PRODUCTS_COLLECTION_URI_INDICATOR = 1;
    private static final int PRODUCT_SINGLE_ITEM_URI_INDICATOR = 2;

    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(ProductsContract.AUTHORITY,
                           ProductsContract.Table.TABLE_NAME,
                           PRODUCTS_COLLECTION_URI_INDICATOR);
        sURIMatcher.addURI(ProductsContract.AUTHORITY,
                           ProductsContract.Table.TABLE_NAME + "/#",
                           PRODUCT_SINGLE_ITEM_URI_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if(SET_KEY_METHOD.equals(method) && !TextUtils.isEmpty(arg)) {
            DatabaseHelper helper = DatabaseHelper.getInstance();
            mWritableDatabase = helper.getWritableDatabase(arg);
            mReadableDatabase = helper.getReadableDatabase(arg);
        }

        return Bundle.EMPTY;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(sURIMatcher.match(uri)) {

            case PRODUCT_SINGLE_ITEM_URI_INDICATOR:
                queryBuilder.setTables(ProductsContract.Table.TABLE_NAME);
                queryBuilder.setProjectionMap(ProductsContract.PROJECTION_MAP);
                queryBuilder.appendWhere(String.format("WHERE %s = %s", ProductsContract.Table._ID, uri.getPathSegments().get(1)));

                break;

            case PRODUCTS_COLLECTION_URI_INDICATOR:
                queryBuilder.setTables(ProductsContract.Table.TABLE_NAME);
                queryBuilder.setProjectionMap(ProductsContract.PROJECTION_MAP);
                break;

            default:
                throw new IllegalArgumentException("Cannot match URI: " + uri.getEncodedPath());
        }

        String orderBy = sortOrder;

        if(TextUtils.isEmpty(orderBy)) {
            orderBy = ProductsContract.DEFAULT_SORT_ORDER;
        }

        final Cursor cursor = queryBuilder.query(mReadableDatabase, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        switch(sURIMatcher.match(uri)) {
            case PRODUCTS_COLLECTION_URI_INDICATOR:
                return ProductsContract.CONTENT_TYPE_COLLECTION;
            case PRODUCT_SINGLE_ITEM_URI_INDICATOR:
                return ProductsContract.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Cannot find type for URI: " + uri.getEncodedPath());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final long rowID = mWritableDatabase.insertOrThrow(ProductsContract.Table.TABLE_NAME, "", values);
        final Uri insertionURI = ContentUris.withAppendedId(uri, rowID);
        getContext().getContentResolver().notifyChange(insertionURI, null);

        return insertionURI;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count = -1;
        switch(sURIMatcher.match(uri)) {

            case PRODUCT_SINGLE_ITEM_URI_INDICATOR:
                 final String rowID = uri.getPathSegments().get(1);
                 count = mWritableDatabase.delete(ProductsContract.Table.TABLE_NAME,
                                                  ProductsContract.Table._ID + " = " + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + " )" : ""),
                                                  selectionArgs);
            break;

            case PRODUCTS_COLLECTION_URI_INDICATOR:
                 count = mWritableDatabase.delete(ProductsContract.Table.TABLE_NAME,
                                                  selection,
                                                  selectionArgs);
            break;

            default:
                throw new IllegalArgumentException("Delete: Could not match URI " + uri.getEncodedPath());
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count = -1;
        
        switch(sURIMatcher.match(uri)) {

            case PRODUCT_SINGLE_ITEM_URI_INDICATOR:
                final String rowID = uri.getPathSegments().get(1);
                count = mWritableDatabase.update(ProductsContract.Table.TABLE_NAME, values,
                                                 ProductsContract.Table._ID + " = " + rowID +
                                                 (!TextUtils.isEmpty(selection) ? " AND (" + selection + " )" : ""),
                                                 selectionArgs);
                break;

            case PRODUCTS_COLLECTION_URI_INDICATOR:
                count = mWritableDatabase.update(ProductsContract.Table.TABLE_NAME,
                                                 values,
                                                 selection,
                                                 selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Delete: Could not match URI " + uri.getEncodedPath());
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
