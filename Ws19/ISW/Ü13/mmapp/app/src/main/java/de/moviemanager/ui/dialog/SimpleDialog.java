package de.moviemanager.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;

public class SimpleDialog extends DialogFragment {
    private static final String TITLE_ARG = "title_arg";
    private static final String MESSAGE_ARG = "message_arg";
    private static final String POSITIVE_ARG = "positive_arg";
    private static final String NEGATIVE_ARG = "negative_arg";
    private static final String DISABLE_NEGATIVE = "disable_negative";

    @Bind(R.id.dialog_title) private TextView title;
    @Bind(R.id.dialog_message) private TextView message;
    @Bind(R.id.positive_button) private Button positive;
    @Bind(R.id.negative_button) private Button negative;

    private Consumer<DialogFragment> onPositive;
    private Consumer<DialogFragment> onNegative;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_simple, container);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        initFromArguments();
        beginListening();
    }

    private void initFromArguments() {
        final Bundle args = getArguments();

        if(args != null) {
            setTitle(args.getString(TITLE_ARG));
            setMessage(args.getString(MESSAGE_ARG));
            setPositiveText(args.getString(POSITIVE_ARG));
            setNegativeText(args.getString(NEGATIVE_ARG));
            if(args.getBoolean(DISABLE_NEGATIVE)) {
                negative.setVisibility(View.GONE);
            }
        }
    }

    private void beginListening() {
        positive.setOnClickListener(view -> {
            if(onPositive != null) {
                onPositive.accept(this);
            }
        });
        negative.setOnClickListener(view -> {
            if(onNegative != null) {
                onNegative.accept(this);
            }
        });
    }

    private void setTitle(final String title) {
        this.title.setText(title);
    }

    private void setMessage(final String message) {
        this.message.setText(message);
    }

    private void setPositiveAction(final Consumer<DialogFragment> action) {
        this.onPositive = action;
    }

    private void setPositiveText(final String positiveText) {
        positive.setText(positiveText);
    }

    private void setNegativeAction(final Consumer<DialogFragment> action) {
        this.onNegative = action;
    }

    private void setNegativeText(final String negativeText) {
        negative.setText(negativeText);
    }

    public static Builder warning(final Context context) {
        return new Builder(context).setTitle(R.string.warning);
    }

    public static Builder error(final Context context) {
        return new Builder(context)
                .setTitle(R.string.error)
                .disableNegative()
                .setCancelable(false);
    }

    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;

        private boolean disableNegative;
        private boolean cancelable;

        private Consumer<DialogFragment> onPositive;
        private Consumer<DialogFragment> onNegative;

        private Builder(final Context context) {
            this.context = context;
            this.disableNegative = false;
            this.cancelable = true;

            positiveButtonText = context.getString(R.string.confirm);
            negativeButtonText = context.getString(R.string.cancel);
        }

        public Builder setTitle(@StringRes final int titleId) {
            this.title = context.getString(titleId);
            return this;
        }

        public Builder setTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(@StringRes final int messageId) {
            this.message = context.getString(messageId);
            return this;
        }

        public Builder setMessage(@StringRes final int messageId, String... args) {
            this.message = String.format(context.getString(messageId), (Object[]) args);
            return this;
        }

        Builder disableNegative() {
            this.disableNegative = true;
            return this;
        }

        Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setPositiveButtonText(@StringRes int textId) {
            this.positiveButtonText = context.getString(textId);
            return this;
        }

        public Builder setPositiveButtonAction(final Consumer<DialogFragment> action) {
            this.onPositive = action;
            return this;
        }

        public Builder setNegativeButtonText(@StringRes int textId) {
            this.negativeButtonText = context.getString(textId);
            return this;
        }

        public Builder setNegativeButtonAction(final Consumer<DialogFragment> action) {
            this.onNegative = action;
            return this;
        }

        public void show() {
            final FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
            final SimpleDialog dialog = new SimpleDialog();
            dialog.setArguments(createArguments());
            dialog.setPositiveAction(onPositive);
            dialog.setNegativeAction(onNegative);
            dialog.setCancelable(cancelable);
            dialog.show(manager, "simple_dialog");
        }

        private Bundle createArguments() {
            final Bundle args = new Bundle();
            args.putString(TITLE_ARG, title);
            args.putString(MESSAGE_ARG, message);
            args.putString(POSITIVE_ARG, positiveButtonText);
            args.putString(NEGATIVE_ARG, negativeButtonText);
            args.putBoolean(DISABLE_NEGATIVE, disableNegative);
            return args;
        }
    }
}
