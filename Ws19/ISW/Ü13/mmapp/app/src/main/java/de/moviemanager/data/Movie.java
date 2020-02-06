package de.moviemanager.data;

import android.os.Parcel;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import de.moviemanager.core.storage.JsonAttr;
import de.util.DateUtils;
import de.util.Pair;
import de.util.SerializablePair;
import de.util.Traits;
import de.util.annotations.Trait;

import static de.util.CollectionUtils.map;
import static de.util.DateUtils.normDate;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 *     Objects of this class will use JSON as storage format. To convert this class to a
 *     {@link org.json.JSONObject} use {@link de.moviemanager.core.json.JsonBridge#toJson(Object)}.
 *     All attributes which are annotated with {@link JsonAttr} will be used. To convert a
 *     {@link org.json.JSONObject} back to a Movie use
 *     {@link de.moviemanager.core.json.JsonBridge#fromJson(JSONObject, Supplier)} with
 *     {@link de.moviemanager.core.json.MovieFromJsonObject}.
 * </p>
 * <p>
 *     <b>!!! </b> If u add or remove attributes to this class you should modify {@link de.moviemanager.core.json.MovieFromJsonObject} too <b>!!!</b>
 * </p>
 *
 */
public class Movie extends Portrayable {
    private static final Traits TRAITS = new Traits(Movie.class);

    @JsonAttr @Trait private Date watchDate;
    @JsonAttr @Trait private Date dueDate;
    @JsonAttr @Trait private String description;
    @JsonAttr @Trait private List<String> languages;
    @JsonAttr @Trait private List<Pair<String, Date>> releases;
    @JsonAttr @Trait private int runtime;
    @JsonAttr @Trait private List<String> productionLocations;
    @JsonAttr @Trait private List<String> filmingLocations;

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public Movie(int id) {
        super(id);
        this.watchDate = null;
        this.dueDate = null;
        this.description = "";
        this.languages = new ArrayList<>();
        this.releases = new ArrayList<>();
        this.runtime = 0;
        this.productionLocations = new ArrayList<>();
        this.filmingLocations = new ArrayList<>();
    }

    private Movie(Parcel in) {
        super(in);
        languages = new ArrayList<>();
        productionLocations = new ArrayList<>();
        filmingLocations = new ArrayList<>();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        description = in.readString();
        in.readStringList(languages);
        releases = map(SerializablePair::toPair, (ArrayList<SerializablePair<String, Date>>) in.readSerializable());
        watchDate = (Date) in.readSerializable();
        dueDate = (Date) in.readSerializable();
        runtime = in.readInt();
        in.readStringList(productionLocations);
        in.readStringList(filmingLocations);
    }

    public String getTitle() {
        return name();
    }

    public void setTitle(String title) {
        setName(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLanguages() {
        return unmodifiableList(languages);
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<Pair<String, Date>> getReleases() {
        return unmodifiableList(releases);
    }

    public void setReleases(final List<Pair<String, Date>> releases) {
        this.releases = releases.stream()
                .map(p -> p.mapSecond(DateUtils::normDate))
                .collect(toList());
    }

    public Date getWatchDate() {
        return watchDate;
    }

    public void setWatchDate(Date watchDate) {
        this.watchDate = normDate(watchDate);
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = normDate(dueDate);
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public List<String> getProductionLocations() {
        return productionLocations;
    }

    public void setProductionLocations(List<String> productionLocations) {
        this.productionLocations = productionLocations;
    }

    public List<String> getFilmingLocations() {
        return unmodifiableList(filmingLocations);
    }

    public void setFilmingLocations(List<String> filmingLocations) {
        this.filmingLocations = filmingLocations;
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
        dest.writeString(description);
        dest.writeStringList(languages);
        dest.writeSerializable(new ArrayList<>(map(SerializablePair::from, releases)));
        dest.writeSerializable(watchDate);
        dest.writeSerializable(dueDate);
        dest.writeInt(runtime);
        dest.writeStringList(productionLocations);
        dest.writeStringList(filmingLocations);
    }
}
