package com.sqlcipherexample.app.database;

import android.content.Context;

import com.sqlcipherexample.app.R;
import com.sqlcipherexample.app.model.contract.ProductsContract;
import com.sqlcipherexample.app.preferences.MySecurePreferences;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String ENCRYPTED_DATABASE_NAME = "my_sqlcipher_database_encrypted.db";
    private static final String UNENCRYPTED_DATABASE_NAME = "my_sqlcipher_database.db";

    private static final int DATABASE_VERSION = 1;

    private static final String[] sTablesArray;

    static {
        sTablesArray = new String[] {
            ProductsContract.Table.TABLE_NAME
        };
    }

    private static DatabaseHelper sDatabaseHelperInstance;

    private final Context mContext;

    private final String SEPARATOR = "#";

    private DatabaseHelper(final Context context) {
        super(context, ENCRYPTED_DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized DatabaseHelper getInstance(final Context context) {
        if(sDatabaseHelperInstance == null) {
            sDatabaseHelperInstance = new DatabaseHelper(context);
        }

        return sDatabaseHelperInstance;
    }

    public static DatabaseHelper getInstance() {
        return sDatabaseHelperInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        setUpTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(database.needUpgrade(newVersion)) {
            String dropQuery = "DROP TABLE IF EXISTS %s;";

            for(String table : sTablesArray) {
                database.execSQL(String.format(dropQuery, table));
            }

            setUpTables(database);
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase(MySecurePreferences.getInstance(mContext).getDatabaseAccessKey());
    }

    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase(MySecurePreferences.getInstance(mContext).getDatabaseAccessKey());
    }

    private String[] getTablesArray() {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openDatabaseMetaResource()));
        String line;
        final StringBuilder queryBuilder = new StringBuilder();

        try {
            while((line = bufferedReader.readLine()) != null) {
                queryBuilder.append(line);
            }
            final String databaseString = queryBuilder.toString();

            return databaseString.split(SEPARATOR);
        } catch(IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(getClass().getSimpleName() + ": cannot create database!");
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) { }
        }
    }

    private final InputStream openDatabaseMetaResource() {
        final InputStream inputStream = mContext.getResources().openRawResource(R.raw.database);

        return inputStream;
    }

    private void setUpTables(SQLiteDatabase db) {
        final String[] tables = getTablesArray();
        for(final String query : tables) {
            db.execSQL(query.trim());
        }
    }

    public static void encryptDatabaseIfUnencrypted(final Context context, final String newDatabaseAccesKey) {
        final File unencryptedDatabaseFile = context.getDatabasePath(UNENCRYPTED_DATABASE_NAME);
        final File encryptedDatabaseFile = context.getDatabasePath(ENCRYPTED_DATABASE_NAME);

        if(unencryptedDatabaseFile.exists() && !encryptedDatabaseFile.exists()) {
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(unencryptedDatabaseFile, "", null);
            database.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';",
                                              unencryptedDatabaseFile.getAbsolutePath(),
                                              newDatabaseAccesKey));
            database.rawExecSQL("SELECT sqlcipher_export('encrypted')");
            database.rawExecSQL("DETACH DATABASE encrypted;");

            int version = database.getVersion();

            database.close();
            context.deleteDatabase(database.getPath());
            final MySecurePreferences securePreferences = MySecurePreferences.getInstance(context);
            database = SQLiteDatabase.openOrCreateDatabase(encryptedDatabaseFile,
                                                           newDatabaseAccesKey,
                                                           null,
                    new SQLiteDatabaseHook() {
                        @Override
                        public void preKey(SQLiteDatabase sqLiteDatabase) {

                        }

                        @Override
                        public void postKey(SQLiteDatabase sqLiteDatabase) {
                            securePreferences.setDatabaseAccessKey(newDatabaseAccesKey);
                        }
                    });
            database.setVersion(version);
            database.close();
        }
    }

    public static void changePassphrase(final String currentPassword, final String newPassword, final Context context) {
        final File databaseFile = context.getDatabasePath(ENCRYPTED_DATABASE_NAME);
        final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile,
                currentPassword,
                null);
        database.execSQL(String.format("PRAGMA rekey = '%s'", newPassword));
        database.close();
    }
}
