package de.moviemanager.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import de.moviemanager.core.storage.JsonAttr;
import de.util.Identifiable;
import de.util.annotations.Trait;

import static java.lang.String.format;

public abstract class Portrayable implements Identifiable, Nameable, Rateable, Parcelable {
    @JsonAttr @Trait private final int id;
    @JsonAttr @Trait private String name;
    @JsonAttr @Trait private int imageId;
    @JsonAttr @Trait private double rating;

    protected Portrayable(int id) {
        this.id = id;
        this.name = "";
        this.imageId = -1;
        this.rating = -1.;
    }

    protected Portrayable(final Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.imageId = in.readInt();
        this.rating = in.readDouble();
    }

    @Override
    public boolean isRated() {
        return !isUnrated();
    }

    @Override
    public boolean isUnrated() {
        return rating < 0;
    }

    public double getRating() {
        double result = -1;
        if(isRated()) {
            result = rating;
        }
        return result;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public double rating() {
        return getRating();
    }

    @NonNull
    @Override
    public String toString() {
        return format("%s{id=%s, name=%s}", getClass().getSimpleName() , id(), name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(imageId);
        dest.writeDouble(rating);
    }
}
