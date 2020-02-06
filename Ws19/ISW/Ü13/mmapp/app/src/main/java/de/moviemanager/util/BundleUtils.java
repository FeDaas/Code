package de.moviemanager.util;

import android.os.Bundle;

public enum BundleUtils {
    ;

    public static String getOrDefault(final Bundle args,
                                final String argName,
                                final String defaultValue) {
        String arg;
        if(args == null) {
            arg = defaultValue;
        } else {
            arg = args.getString(argName);
        }

        return arg;
    }

    public static boolean getOrDefault(final Bundle args,
                                 final String argName,
                                 final boolean defaultValue) {
        boolean arg;
        if(args == null) {
            arg = defaultValue;
        } else {
            arg = args.getBoolean(argName, defaultValue);
        }

        return arg;
    }
}
