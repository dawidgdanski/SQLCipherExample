package com.sqlcipherexample.app.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.sqlcipherexample.app.model.contract.ProductsContract;

public class Product {

    private final long id;
    private final int status;
    private final String name;

    public Product(long id, String name, final int status) {
        this.id = id;
        this.status = status;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(ProductsContract.Table.NAME, name);
        values.put(ProductsContract.Table.STATUS, status);

        return values;
    }

    public static Product fromCursor(final Cursor cursor) {
        if(cursor.moveToFirst()) {
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(ProductsContract.Table._ID));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductsContract.Table.NAME));
            final int status = cursor.getInt(cursor.getColumnIndexOrThrow(ProductsContract.Table.STATUS));
            return new Product(id, name, status);
        }

        throw new IllegalStateException("Cannot create Product. Cursor does not contain any results.");
    }

    @Override
    public boolean equals(Object o) {
        try {
            final Product product = (Product) o;
            return id == product.getId();
        } catch(Exception e) {
            return false;
        }
    }


}
