package de.moviemanager.util;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.SearchView.OnQueryTextListener;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Listeners {
    private Listeners() {}

    public static TextWatcher createOnTextChangedListener(final Consumer<CharSequence> action) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no action to perform
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                action.accept(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no action to perform
            }
        };
    }

    public static OnQueryTextListener liveQueryListener(final Activity activity,
                                                        final Predicate<String> onQueryChange) {
        return new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                AndroidUtils.closeKeyboard(activity);
                return onQueryTextChange(query);
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                return onQueryChange.test(query);
            }
        };
    }

    public static OnQueryTextListener submitQueryListener(final Activity activity,
                                                        final Predicate<String> onQuerySubmit) {
        return new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                AndroidUtils.closeKeyboard(activity);
                return onQuerySubmit.test(query);
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                return false;
            }
        };
    }
}
