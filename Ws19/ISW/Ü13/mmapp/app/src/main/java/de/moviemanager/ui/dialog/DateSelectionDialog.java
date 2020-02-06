package de.moviemanager.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;

public class DateSelectionDialog extends DialogFragment {
    private static final String ARG_INITIAL_DATE = "initial_data";
    private static final String ARG_HAS_MIN_DATE = "has_min_date";
    private static final String ARG_MIN_DATE = "min_date";
    private static final String ARG_HAS_MAX_DATE = "has_max_date";
    private static final String ARG_MAX_DATE = "max_date";

    private final Calendar calendar;
    private Consumer<Date> dateChangeListener = d -> {
    };
    @Bind(R.id.calendar) private DatePicker picker;
    @Bind(R.id.positive_button) private Button positiveButton;
    @Bind(R.id.negative_button) private Button negativeButton;

    public static DateSelectionDialog create(final Date initial, final Date minDate, final Date maxDate) {
        final DateSelectionDialog frag = new DateSelectionDialog();
        final Bundle args = new Bundle();
        if (initial != null)
            args.putLong(ARG_INITIAL_DATE, initial.getTime());
        if (minDate != null) {
            args.putBoolean(ARG_HAS_MIN_DATE, true);
            args.putLong(ARG_MIN_DATE, minDate.getTime());
        }
        if (maxDate != null) {
            args.putBoolean(ARG_HAS_MAX_DATE, true);
            args.putLong(ARG_MAX_DATE, maxDate.getTime());
        }
        frag.setArguments(args);
        return frag;
    }

    private DateSelectionDialog() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_date_selection, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        setMembersFromArguments();
        setupUI();
    }


    private void setMembersFromArguments() {
        final Bundle args = getArguments();

        calendar.setTimeInMillis(args.getLong(ARG_INITIAL_DATE, calendar.getTimeInMillis()));

        if (args.getBoolean(ARG_HAS_MIN_DATE, false))
            picker.setMinDate(args.getLong(ARG_MIN_DATE));

        if (args.getBoolean(ARG_HAS_MAX_DATE, false))
            picker.setMaxDate(args.getLong(ARG_MAX_DATE));
    }

    private void setupUI() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        picker.updateDate(year, month, day);

        positiveButton.setOnClickListener(this::onButtonClick);
        negativeButton.setOnClickListener(this::onButtonClick);
    }

    private void onButtonClick(final View view) {
        int id = view.getId();

        if (id == R.id.positive_button) {
            dateChangeListener.accept(getDateFromDatePicker(picker));
            dismiss();
        } else if (id == R.id.negative_button) {
            dateChangeListener.accept(null);
            dismiss();
        }
    }

    private Date getDateFromDatePicker(final DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        calendar.set(year, month, day, 0, 0, 0);

        return calendar.getTime();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                onButtonClick(negativeButton);
                dismiss();
            }
        };
    }

    public void setDateChangeListener(@NonNull Consumer<Date> listener) {
        dateChangeListener = listener;
    }
}