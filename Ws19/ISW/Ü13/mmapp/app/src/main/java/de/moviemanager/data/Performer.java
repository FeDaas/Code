package de.moviemanager.data;

import android.os.Parcel;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.moviemanager.core.storage.JsonAttr;
import de.util.DateUtils;
import de.util.Traits;
import de.util.annotations.Trait;

import static de.util.DateUtils.normDate;
import static de.util.DateUtils.now;

public class Performer extends Portrayable {
    private static final Traits TRAITS = new Traits(Performer.class);

    public static final Creator<Performer> CREATOR = new Creator<Performer>() {
        @Override
        public Performer createFromParcel(Parcel source) {
            return new Performer(source);
        }

        @Override
        public Performer[] newArray(int size) {
            return new Performer[size];
        }
    };

    @JsonAttr @Trait private String birthName;
    @JsonAttr @Trait private String biography;
    @JsonAttr @Trait private Date dateOfBirth;
    @JsonAttr @Trait private List<String> occupations;

    public Performer(int id) {
        super(id);
        this.birthName = "";
        this.biography = "";
        this.dateOfBirth = null;
        this.occupations = new ArrayList<>();
    }

    private Performer(Parcel in) {
        super(in);
        this.occupations = new ArrayList<>();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        birthName = in.readString();
        biography = in.readString();
        dateOfBirth = (Date) in.readSerializable();
        in.readStringList(occupations);
    }

    public String getName() {
        return name();
    }

    public String getFirstName() {
        String[] parts = name().split(" ");
        if(parts.length > 1)
            return parts[0];
        return name();
    }

    public List<String> getMiddleNames() {
        String[] parts = name().split(" ");
        if(parts.length > 2)
            return Arrays.asList(parts).subList(1, parts.length - 2);
        return Collections.emptyList();
    }

    public String getLastName() {
        String[] parts = name().split(" ");
        if(parts.length > 1)
            return parts[parts.length - 1];
        return name();
    }

    public String getBirthName() {
        return birthName;
    }

    public void setBirthName(String birthName) {
        this.birthName = birthName;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = normDate(dateOfBirth);
    }

    public int age() {
        if(dateOfBirth == null)
            return 0;
        return DateUtils.differenceInYears(now(), dateOfBirth);
    }

    public List<String> getOccupations() {
        return occupations;
    }

    public void setOccupations(List<String> occupations) {
        this.occupations = occupations;
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(birthName);
        dest.writeString(biography);
        dest.writeSerializable(dateOfBirth);
        dest.writeStringList(occupations);
    }
}
