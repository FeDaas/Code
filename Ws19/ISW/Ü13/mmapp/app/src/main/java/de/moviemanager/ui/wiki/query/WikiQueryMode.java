package de.moviemanager.ui.wiki.query;

import android.content.Intent;

public enum WikiQueryMode {
    UNDEFINED, ACTOR, FILM;

    public static WikiQueryMode fromExtra(Intent intent, String field) {
        int defaultOrdinal = WikiQueryMode.UNDEFINED.ordinal();
        int ordinal = intent.getIntExtra(field, defaultOrdinal);
        return WikiQueryMode.values()[ordinal];
    }
}
