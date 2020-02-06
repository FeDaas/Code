package de.moviemanager.ui.detail.modifications;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;

import java.util.function.BiConsumer;

import de.moviemanager.ui.detail.modifiable.ModifiableContext;

import static de.moviemanager.util.ScrollViewUtils.scrollToViewIfNeeded;

public class EditTextModification implements OnFocusChangeListener {
    private final ModifiableContext modContext;
    private final ScrollView scrollView;
    private final EditText edit;
    private final BiConsumer<EditText, String> validateNewState;

    public static void apply(ModifiableContext modContext, ScrollView scrollView, EditText edit) {
        apply(modContext, scrollView, edit, (e, s) -> {});
    }

    public static void apply(ModifiableContext modContext, ScrollView scrollView, EditText edit, BiConsumer<EditText, String> validateNewState) {
        new EditTextModification(modContext, scrollView, edit, validateNewState);
    }

    private EditTextModification(ModifiableContext modContext, ScrollView scrollView, EditText edit, BiConsumer<EditText, String> validateNewState) {
        this.modContext = modContext;
        this.scrollView = scrollView;
        this.edit = edit;
        this.edit.setOnFocusChangeListener(this);
        this.validateNewState = validateNewState;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            final String old = edit.getText().toString();
            modContext.addModification(new Modification<>(old, this::undo));
        } else {
            final String newState = edit.getText().toString();
            handleNewState(newState);
        }
    }

    private void undo(String s) {
        edit.setText(s);
        if(scrollView != null)
            scrollView.post(() -> scrollToViewIfNeeded(scrollView, edit));
        validateNewState.accept(edit, s);
    }

    private void handleNewState(String newState) {
        if (modContext.getLastModification().getOldState().equals(newState))
            modContext.removeLastModification();
        else
            validateNewState.accept(edit, newState);
    }

}