package de.moviemanager.data;

public interface Rateable {
    boolean isUnrated();
    boolean isRated();
    double rating();
}
