package de.moviemanager.ui.detail.modifiable;

import android.widget.ScrollView;

import androidx.annotation.IdRes;

import java.util.Arrays;
import java.util.List;

import de.moviemanager.util.AndroidStringUtils;

public class StringListAttribute<X> extends TextAttribute<X, List<String>> {
    public StringListAttribute(final ModifiableAppCompatActivity context,
                               final ScrollView root,
                               @IdRes int editTextId) {
        super(context, root, editTextId);
    }

    @Override
    protected String toString(final List<String> content) {
        return AndroidStringUtils.join("\n", content);
    }

    @Override
    protected List<String> fromString(final String string) {
        return Arrays.asList(string.split("\n"));
    }
}
