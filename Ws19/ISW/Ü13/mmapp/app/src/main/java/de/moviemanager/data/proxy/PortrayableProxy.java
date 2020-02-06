package de.moviemanager.data.proxy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

public interface PortrayableProxy extends Parcelable {
    String getName();
    Drawable getImage(Context context);

    boolean isOfType(Class<?> clazz);

    boolean isTemporary();
    boolean isPersistent();
}
