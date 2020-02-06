package de.moviemanager.ui.adapter.base;

import android.widget.Filterable;

public interface DirectFilterable extends Filterable {
    void filter(final CharSequence constraint);
}
