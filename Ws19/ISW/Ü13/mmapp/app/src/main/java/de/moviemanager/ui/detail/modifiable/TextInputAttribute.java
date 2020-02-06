package de.moviemanager.ui.detail.modifiable;

import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.IdRes;

import com.google.android.material.textfield.TextInputLayout;

import java.util.function.BiConsumer;

import de.moviemanager.ui.detail.modifications.EditTextModification;
import de.moviemanager.util.Listeners;

public class TextInputAttribute<X> extends ModifiableAttribute<X, String> {
    private final int id;
    private final ScrollView scrollView;
    private TextInputLayout layout;
    private EditText editText;
    private TextWatcher textChangeListener;
    private BiConsumer<EditText, String> inputValidation;

    public TextInputAttribute(ModifiableAppCompatActivity modContext, ScrollView scrollView, @IdRes int id) {
        super(modContext);
        this.scrollView = scrollView;
        this.id = id;
        this.textChangeListener = Listeners.createOnTextChangedListener(s -> {
        });
        this.inputValidation = (e, s) -> {
        };
    }

    @Override
    public void bindViews() {
        layout = getContext().findViewById(id);
        editText = layout.getEditText();
    }

    void setTextChangeListener(TextWatcher textChangeListener) {
        this.textChangeListener = textChangeListener;
    }

    void setInputValidation(BiConsumer<EditText, String> inputValidation) {
        this.inputValidation = inputValidation;
    }

    @Override
    public void bindListeners() {
        EditTextModification.apply(getContext(), scrollView, editText, inputValidation);
        editText.addTextChangedListener(textChangeListener);
    }

    @Override
    protected void setContent(String content) {
        editText.setText(content);
    }

    public boolean hasEmptyText() {
        return getContent().trim().isEmpty();
    }

    @Override
    public String getContent() {
        return editText.getText().toString();
    }

    public static class Builder<X> extends BaseBuilder<X, String, TextInputAttribute<X>> {
        private TextWatcher textChangeListener;
        private BiConsumer<EditText, String> inputValidation;

        public Builder(ModifiableAppCompatActivity context) {
            super(context);
        }

        public Builder<X> setTextChangeListener(TextWatcher textChangeListener) {
            this.textChangeListener = textChangeListener;
            return this;
        }

        public Builder<X> setInputValidation(BiConsumer<EditText, String> inputValidation) {
            this.inputValidation = inputValidation;
            return this;
        }

        @Override
        protected void callAdditionalSetters(TextInputAttribute<X> attr) {
            attr.setTextChangeListener(textChangeListener);
            attr.setInputValidation(inputValidation);
        }
    }

    public void setError(final String msg) {
        layout.setErrorEnabled(msg != null);
        layout.setError(msg);
    }
}
