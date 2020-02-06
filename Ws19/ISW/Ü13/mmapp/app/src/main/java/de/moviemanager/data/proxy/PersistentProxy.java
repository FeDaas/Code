package de.moviemanager.data.proxy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import androidx.annotation.Nullable;

import java.util.Objects;

import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.data.ImagePyramid;
import de.moviemanager.data.Portrayable;
import de.util.Traits;
import de.util.annotations.Trait;

public class PersistentProxy implements PortrayableProxy {
    private static final RuntimeStorageAccess STORAGE = RuntimeStorageAccess.getInstance();
    private static final Traits TRAITS = new Traits(PersistentProxy.class);
    @Trait private final Portrayable source;

    public static final Creator<PersistentProxy> CREATOR = new Creator<PersistentProxy>() {
        @Override
        public PersistentProxy createFromParcel(Parcel source) {
            return new PersistentProxy(source);
        }

        @Override
        public PersistentProxy[] newArray(int size) {
            return new PersistentProxy[size];
        }
    };

    public PersistentProxy(final Portrayable source) {
        this.source = Objects.requireNonNull(source);
    }

    private PersistentProxy(final Parcel in) {
        source = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public String getName() {
        return source.name();
    }

    @Override
    public Drawable getImage(Context context) {
        return STORAGE.getImage(context, source, ImagePyramid.ImageSize.LARGE).first;
    }

    @Override
    public boolean isOfType(Class<?> clazz) {
        return clazz.isInstance(source);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public Portrayable getSource() {
        return source;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createImmutableHashFor(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, int flags) {
        parcel.writeParcelable(source, flags);
    }
}
