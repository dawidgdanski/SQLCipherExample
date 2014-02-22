package com.sqlcipherexample.app;

import android.os.Build;

public final class Utils {
    public static boolean isAPIVersionEqualOrHigherThan(final int apiVersion) {
        return Build.VERSION.SDK_INT == apiVersion;
    }

    public static boolean isAPIVersionLowerThan(final int apiVersion) {
        return ! isAPIVersionEqualOrHigherThan(apiVersion);
    }
}
