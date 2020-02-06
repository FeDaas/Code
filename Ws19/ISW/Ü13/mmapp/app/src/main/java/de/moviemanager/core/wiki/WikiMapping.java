package de.moviemanager.core.wiki;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import de.moviemanager.data.Movie;
import de.moviemanager.data.Performer;
import de.util.Pair;
import de.wiki.data.Actor;
import de.wiki.data.Film;

import static java.util.stream.Collectors.toList;

final class WikiMapping {
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private WikiMapping() {}

    public static void insertNonEmptyFieldsInto(Film f, Movie m) {
        insertIfNotEmpty(m::setTitle, f.getTitle());
        insertIfNotEmpty(m::setLanguages, f.getLanguages());
        insertIfNotEmpty(m::setDescription, f.getDescription());
        insertIfNotEmpty(m::setRuntime, Integer.parseInt(f.getRunningTime().split(" ")[0]));
        insertIfNotEmpty(m::setReleases, parseReleases(f.getReleaseDates()));
    }

    public static void insertNonEmptyFieldsInto(Actor a, Performer p) {
        insertIfNotEmpty(p::setName, a.getName());
        insertIfNotEmpty(p::setBirthName, a.getBirthName());
        insertIfNotEmpty(p::setBiography, a.getBiography());
        insertIfNotEmpty(p::setOccupations, a.getOccupations());
        insertIfNotEmpty(p::setDateOfBirth, parseDateFromString(a.getDateOfBirth()));
    }

    private static List<Pair<String, Date>> parseReleases(List<Pair<String, String>> releases) {
        return releases
                .stream()
                .map(p -> p.mapSecond(WikiMapping::parseDateFromString))
                .collect(toList());
    }

    private static Date parseDateFromString(String s) {
        try {
            final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            return formatter.parse(s);
        } catch (ParseException e) {
            Date d = new Date();
            d.setTime(0L);
            return d;
        }
    }

    private static void insertIfNotEmpty(Consumer<String> setter, String newObject) {
        if (!newObject.isEmpty()) {
            setter.accept(newObject);
        }
    }

    private static <X> void insertIfNotEmpty(Consumer<List<X>> setter, List<X> newObject) {
        if (!newObject.isEmpty()) {
            setter.accept(newObject);
        }
    }

    private static <T> void insertIfNotEmpty(Consumer<T> setter, T newObject) {
        if (newObject != null) {
            setter.accept(newObject);
        }
    }

}
