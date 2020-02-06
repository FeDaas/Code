package de.moviemanager.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public enum  DimensionUtils {
    ;

    public static float dpToPixels(Context context, int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public static float spToPixels(Context context, int sp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                r.getDisplayMetrics()
        );
    }
}
