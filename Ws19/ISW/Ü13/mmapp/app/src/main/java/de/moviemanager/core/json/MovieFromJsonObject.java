package de.moviemanager.core.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.moviemanager.data.Movie;
import de.util.Pair;

import static de.util.Pair.MAP_KEY_FIRST;
import static de.util.Pair.MAP_KEY_SECOND;
import static de.util.Pair.paired;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class MovieFromJsonObject extends FromJsonObject<Movie> {

    public MovieFromJsonObject() {
        super(MovieFromJsonObject::fromMap);
        registerSetter("name", Movie::setTitle);
        registerSetter("description", Movie::setDescription);
        registerSetter("languages", Movie::setLanguages);
        registerSetter("releases", Movie::setReleases);
        registerSetter("watchDate", Movie::setWatchDate);
        registerSetter("dueDate", Movie::setDueDate);
        registerSetter("runtime", Movie::setRuntime);
        registerSetter("productionLocations", Movie::setProductionLocations);
        registerSetter("filmingLocations", Movie::setFilmingLocations);
        registerSetter("rating", MovieFromJsonObject::setRating);
        registerSetter("imageId", Movie::setImageId);

        registerConversion("watchDate", MovieFromJsonObject::convertStringToDate);
        registerConversion("dueDate", MovieFromJsonObject::convertStringToDate);
        registerConversion("languages", MovieFromJsonObject::convertJSONArrayToListOfStrings);
        registerConversion("productionLocations", MovieFromJsonObject::convertJSONArrayToListOfStrings);
        registerConversion("filmingLocations", MovieFromJsonObject::convertJSONArrayToListOfStrings);
        registerConversion("releases", MovieFromJsonObject::convertJSONArrayToReleases);
    }

    private static Movie fromMap(Map<String, Object> map) {
        int id = (int) map.get("id");
        return new Movie(id);
    }

    private static void setRating(Movie movie, Number number) {
        movie.setRating(number.doubleValue());
    }

    private static List<Pair<String, Date>> convertJSONArrayToReleases(String s) {
        JSONArray array;
        try {
            array = new JSONArray(s);
        } catch (JSONException e) {
            return new ArrayList<>();
        }

        return range(0, array.length())
                .mapToObj(array::optJSONObject)
                .map(obj -> paired(obj.optString(MAP_KEY_FIRST), obj.optString(MAP_KEY_SECOND)))
                .map(p -> p.mapSecond(MovieFromJsonObject::convertStringToDate))
                .collect(toList());
    }
}
