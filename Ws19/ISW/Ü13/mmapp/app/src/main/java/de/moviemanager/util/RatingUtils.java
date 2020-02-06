package de.moviemanager.util;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.moviemanager.core.storage.RuntimeStorageAccess;
import de.moviemanager.core.storage.RuntimeStorageConcept;
import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.moviemanager.data.Rateable;

public enum RatingUtils {
    ;

    private static RuntimeStorageConcept storage = RuntimeStorageAccess.getInstance();

    static void mockStorage(final RuntimeStorageConcept storage) {
        RatingUtils.storage = storage;
    }

    static void removeMock() {
        RatingUtils.storage = RuntimeStorageAccess.getInstance();
    }

    public static double calculateOverallRating(Movie m) {
        List<Performer> performers = storage.getLinkedPerformersOfMovie(m);
        return calculateOverallRating(m, performers);
    }

    static double calculateOverallRating(final Movie m, final List<Performer> performers) {
        double movieRating = m.rating();
        double result = m.rating();

        if(!performers.isEmpty() && m.isRated()) {
            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            for (Performer performer : performers) {
                if (performer.isRated()) {
                    double rating = performer.rating();
                    stats.accept(rating);
                }
            }

            if(stats.getCount() > 0) {
                double performersRating = stats.getAverage();
                result = (movieRating + performersRating) / 2.0;
            }
        }

        return result;
    }

    public static String ratingToString(double value) {
        return ratingToString(value, false);
    }

    public static String ratingToString(double value, boolean showExact) {
        String sub;
        if (value < 0) {
            sub = "Not Rated";
        } else {
            sub = RatingUtils.textRatingBar(value, 5);
            if (showExact) {
                sub += String.format(Locale.US, " (%2.1f)", value);
            }
        }
        return sub;
    }

    static String textRatingBar(double d, int maxStars) {
        final StringBuilder builder = new StringBuilder(maxStars);
        for (int i = 0; i < maxStars; ++i) {
            builder.append(selectStar(d, i));
        }

        return builder.toString();
    }

    private static String selectStar(double value, int offset) {
        if (value < 0.5 + offset) {
            return "\u2606";
        } else if (value < 0.9 + offset) {
            return "\u2bea";
        } else {
            return "\u2605";
        }
    }
}
