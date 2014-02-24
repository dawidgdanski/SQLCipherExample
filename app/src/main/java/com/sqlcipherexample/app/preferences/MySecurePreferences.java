package com.sqlcipherexample.app.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sqlcipherexample.app.exception.EncryptionException;
import com.sqlcipherexample.app.exception.SecurePreferencesException;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class MySecurePreferences {

    private static final String PREFERENCES_NAME = "com.sqlcipherexample.app";

    private static MySecurePreferences sInstance;

    private final String KEY_TAG = "my access key";

    private final SharedPreferences mSharedPreferences;

    private static final byte[] SECRET_KEY = {
            0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79
    };

    private MySecurePreferences(final Context context) {
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME,
                                                                                  Context.MODE_PRIVATE);
    }

    public static MySecurePreferences getInstance(final Context context) {
            if(sInstance == null) {
                sInstance = new MySecurePreferences(context);
            }

            return sInstance;
    }

    public boolean containsKey(final String key) {
        return mSharedPreferences.contains(key);
    }

    public boolean isContainsDatabaseAccessKey() {
        return containsKey(KEY_TAG);
    }

    public String getDatabaseAccessKey() {
         return getString(KEY_TAG);
    }

    public void setDatabaseAccessKey(final String newDatabaseAccessKey) {
        setString(KEY_TAG, newDatabaseAccessKey);
    }

    public void setString(final String tag, final String value) {

        if(TextUtils.isEmpty(value)) {
            throw new SecurePreferencesException("Empty value applied!");
        }

        mSharedPreferences.edit()
                .putString(tag, encrypt(value))
                .commit();
    }

    public String getString(final String tag) {
        return decrypt(mSharedPreferences.getString(tag, ""));
    }

    private String encrypt(final String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            final SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.encodeBase64(cipher.doFinal(password.getBytes()));
            return new String(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    private String decrypt(final String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            final SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            final String decryptedPassword = new String(cipher.doFinal(Base64.decodeBase64(encryptedPassword.getBytes())));
            return decryptedPassword;
        } catch (Exception e) {
            throw new EncryptionException(e.getMessage());
        }
    }

}
