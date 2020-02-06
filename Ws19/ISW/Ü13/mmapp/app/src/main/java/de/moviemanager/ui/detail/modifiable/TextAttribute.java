package de.moviemanager.ui.detail.modifiable;

import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.IdRes;

import de.moviemanager.ui.detail.modifications.EditTextModification;

import static de.moviemanager.util.ScrollViewUtils.enableDeepScroll;

public abstract class TextAttribute <X, T> extends ModifiableAttribute<X, T> {
    private final int editTextId;
    private final ScrollView root;
    private EditText editList;

    TextAttribute(final ModifiableAppCompatActivity context,
                         final ScrollView root,
                         @IdRes int editTextId) {
        super(context);

        this.editTextId = editTextId;
        this.root = root;
    }

    @Override
    public void bindViews() {
        editList = getContext().findViewById(editTextId);
        enableDeepScroll(editList);
    }

    @Override
    public void bindListeners() {
        EditTextModification.apply(getContext(), root, editList);
    }

    @Override
    protected void setContent(T content) {
        editList.setText(toString(content));
    }

    protected abstract String toString(final T content);

    @Override
    public T getContent() {
        return fromString(editList.getText().toString());
    }

    protected abstract T fromString(final String string);
}