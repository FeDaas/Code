package de.moviemanager.ui.detail.modifiable;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayDeque;
import java.util.Deque;

import de.moviemanager.android.ResultHandlingActivity;
import de.moviemanager.ui.detail.modifications.Modification;

import static java.util.Optional.ofNullable;

public abstract class ModifiableAppCompatActivity
        extends ResultHandlingActivity
        implements ModifiableContext {
    private final Deque<Modification<Object>> modifications;

    public ModifiableAppCompatActivity() {
        modifications = new ArrayDeque<>();
    }

    public void addModification(final Modification<?> mod) {
        modifications.push((Modification<Object>) mod);
        onModificationsChanged();
    }

    protected abstract void onModificationsChanged();

    public Modification<Object> getLastModification() {
        return this.modifications.peek();
    }

    public Modification<Object> removeLastModification() {
        final Modification<Object> mod = this.modifications.pop();
        onModificationsChanged();
        return mod;
    }

    public boolean hasModifications() {
        return !modifications.isEmpty();
    }

    @Override
    public int getModificationCount() {
        return modifications.size();
    }

    @Override
    public void hideKeyboard() {
        final Object systemService = getSystemService(Activity.INPUT_METHOD_SERVICE);
        final InputMethodManager imm = (InputMethodManager) systemService;
        final View view = ofNullable(getCurrentFocus()).orElse(new View(this));
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }
}
