package com.sqlcipherexample.app.model.contract;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

public class ProductsContract {

    private ProductsContract() {}

    public static final String AUTHORITY = "com.sqlcipherexample.app.provider.MyContentProvider";

    public static final String CONTENT_TYPE_COLLECTION = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.contentprovider.products";
    public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.contentprovider.product";

    public static final String DEFAULT_SORT_ORDER = Table.NAME + " ASC";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Table.TABLE_NAME);

    public static final Map<String, String> PROJECTION_MAP = new HashMap<String, String>();

    public static final int PRODUCT_STATUS_TO_SEND = 1;

    static {
        PROJECTION_MAP.put(Table._ID, Table._ID);
        PROJECTION_MAP.put(Table.NAME, Table.NAME);
    }

    public static class Table implements BaseColumns {
        public static final String TABLE_NAME = "product";

        public static final String NAME = "name";
        public static final String STATUS = "status";
    }

}
