package de.moviemanager.data.proxy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import androidx.annotation.Nullable;

import de.moviemanager.data.Portrayable;
import de.util.Traits;
import de.util.annotations.Trait;

import static java.util.Optional.ofNullable;

public class TemporaryProxy implements PortrayableProxy {
    private static final Traits TRAITS = new Traits(TemporaryProxy.class);
    @Trait private final Class<? extends Portrayable> type;
    @Trait private final String name;
    @Trait private final Bitmap image;
    @Trait private String creatorKey;
    @Trait private int persistentId = -1;

    public static final Creator<TemporaryProxy> CREATOR = new Creator<TemporaryProxy>() {
        @Override
        public TemporaryProxy createFromParcel(Parcel source) {
            return new TemporaryProxy(source);
        }

        @Override
        public TemporaryProxy[] newArray(int size) {
            return new TemporaryProxy[size];
        }
    };

    private TemporaryProxy(final Parcel in) {
        final ClassLoader loader = getClass().getClassLoader();
        type = (Class<? extends Portrayable>) in.readSerializable();
        name = in.readString();
        image = in.readParcelable(loader);
        creatorKey = in.readString();
        persistentId = in.readInt();
    }

    public TemporaryProxy(final Class<? extends Portrayable> type,
                          final String name,
                          final Drawable image) {
        this(type, name, ((BitmapDrawable) image).getBitmap());
    }

    private TemporaryProxy(final Class<? extends Portrayable> type,
                           final String name,
                           final Bitmap image) {
        this.type = type;
        this.name = name;
        this.image = image;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Drawable getImage(Context context) {
        return new BitmapDrawable(context.getResources(), image);
    }

    @Override
    public boolean isOfType(Class<?> clazz) {
        return clazz.isAssignableFrom(type);
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    public boolean hasCreatorKey() {
        return creatorKey != null;
    }

    public void setCreatorKey(final String creatorKey) {
        this.creatorKey = creatorKey;
    }

    public String getCreatorKey() {
        return this.creatorKey;
    }

    public void basedOn(Portrayable p) {
        persistentId = p.id();
    }

    public boolean matches(Portrayable portrayable) {
        return ofNullable(portrayable)
                .map(Portrayable::id)
                .filter(id -> id == persistentId)
                .isPresent();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(type);
        parcel.writeString(name);
        parcel.writeParcelable(image, flags);
        parcel.writeString(creatorKey);
        parcel.writeInt(persistentId);
    }
}
