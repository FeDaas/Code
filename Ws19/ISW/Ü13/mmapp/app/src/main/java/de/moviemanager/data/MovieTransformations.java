package de.moviemanager.data;

import java.util.Date;
import java.util.List;

import de.util.Pair;
import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

import static de.util.operationflow.ReversibleOperations.reversibleTransformation;

public final class MovieTransformations {
    private MovieTransformations() {

    }

    public static ReversibleTransformation<Movie> setTitle(String title) {
        return reversibleTransformation(
                Movie::getTitle,
                Movie::setTitle,
                title
        );
    }

    public static ReversibleTransformation<Movie> setLanguages(List<String> languages) {
        return reversibleTransformation(
                Movie::getLanguages,
                Movie::setLanguages,
                languages
        );
    }

    public static ReversibleTransformation<Movie> setReleases(List<Pair<String, Date>> releases) {
        return reversibleTransformation(
                Movie::getReleases,
                Movie::setReleases,
                releases
        );
    }

    public static ReversibleTransformation<Movie> setRuntime(int runtime) {
        return reversibleTransformation(
                Movie::getRuntime,
                Movie::setRuntime,
                runtime
        );
    }

    public static ReversibleTransformation<Movie> setDescription(String description) {
        return reversibleTransformation(
                Movie::getDescription,
                Movie::setDescription,
                description
        );
    }

    public static ReversibleTransformation<Movie> setWatchDate(Date watchDate) {
        return reversibleTransformation(
                Movie::getWatchDate,
                Movie::setWatchDate,
                watchDate
        );
    }

    public static ReversibleTransformation<Movie> setDueDate(Date dueDate) {
        return reversibleTransformation(
                Movie::getDueDate,
                Movie::setDueDate,
                dueDate
        );
    }

    public static ReversibleTransformation<Movie> setProductionLocations(List<String> productionLocations) {
        return reversibleTransformation(
                Movie::getProductionLocations,
                Movie::setProductionLocations,
                productionLocations
        );
    }

    public static ReversibleTransformation<Movie> setFilmingLocations(List<String> filmingLocations) {
        return reversibleTransformation(
                Movie::getFilmingLocations,
                Movie::setFilmingLocations,
                filmingLocations
        );
    }

    public static ReversibleTransformation<Movie> setRating(double rating) {
        return reversibleTransformation(
                Movie::getRating,
                Movie::setRating,
                rating
        );
    }

    public static ReversibleTransformation<Movie> setImageId(int imageId) {
        return reversibleTransformation(
                Movie::getImageId,
                Movie::setImageId,
                imageId
        );
    }
}
