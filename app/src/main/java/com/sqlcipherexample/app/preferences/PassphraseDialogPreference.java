package com.sqlcipherexample.app.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sqlcipherexample.app.R;
import com.sqlcipherexample.app.database.DatabaseHelper;
import com.sqlcipherexample.app.model.contract.ProductsContract;
import com.sqlcipherexample.app.provider.MyContentProvider;

public class PassphraseDialogPreference extends DialogPreference {

    private EditText mCurrentPasswordText;

    private EditText mNextPasswordText;

    public PassphraseDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PassphraseDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mCurrentPasswordText = (EditText) view.findViewById(R.id.current_password);
        mNextPasswordText = (EditText) view.findViewById(R.id.new_password);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if(positiveResult) {
            onDatabasePassphraseChange(mCurrentPasswordText.getText().toString(),
                                       mNextPasswordText.getText().toString());
        }
    }

    private void onDatabasePassphraseChange(final String currentPassword, final String newPassword) {
        Context context = getContext();
        try {
            DatabaseHelper.changePassphrase(currentPassword, newPassword, context);
            MySecurePreferences.getInstance(context).setDatabaseAccessKey(newPassword);
            Toast.makeText(context, R.string.password_changed, Toast.LENGTH_LONG).show();
            context.getContentResolver().call(ProductsContract.CONTENT_URI,
                                              MyContentProvider.SET_KEY_METHOD,
                                              newPassword,
                                              Bundle.EMPTY);
        } catch(Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
