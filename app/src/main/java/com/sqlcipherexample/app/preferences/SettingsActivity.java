package com.sqlcipherexample.app.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.sqlcipherexample.app.R;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
