package de.moviemanager.ui.detail.modifiable;

import android.widget.ScrollView;

import androidx.annotation.IdRes;

public class FreeTextAttribute<X> extends TextAttribute<X, String> {
    public FreeTextAttribute(final ModifiableAppCompatActivity context,
                             final ScrollView root,
                             @IdRes int editTextId) {
        super(context, root, editTextId);
    }

    @Override
    protected String toString(String content) {
        return content;
    }

    @Override
    protected String fromString(String string) {
        return string;
    }
}