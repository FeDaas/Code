package de.moviemanager.ui.dialog;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.util.AndroidUtils;
import de.moviemanager.util.autobind.AutoBind;
import de.moviemanager.util.autobind.Bind;
import de.util.DateUtils;
import de.util.Month;
import de.util.Pair;
import de.util.SerializablePair;

import static de.moviemanager.util.AndroidUtils.closeKeyboard;
import static de.util.Pair.paired;
import static java.util.Optional.ofNullable;

public class ReleaseDialog
        extends DialogFragment
        implements OnClickListener, OnValueChangeListener {
    private static final String RELEASE_DATA = "release_data";
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;
    private static final int MIN_MONTH = 0;
    private static final int MAX_MONTH = 11;
    private static final int MIN_YEAR = 1895;
    private static final int MAX_YEAR = 2100;

    @Bind(R.id.dialog_title) private TextView showTitle;
    @Bind(R.id.edit_release_day) private NumberPicker dayPicker;
    @Bind(R.id.edit_release_month) private NumberPicker monthPicker;
    @Bind(R.id.edit_release_year) private NumberPicker yearPicker;
    @Bind(R.id.edit_release_name) private TextInputLayout editName;
    @Bind(R.id.positive_button) private Button confirmRelease;
    @Bind(R.id.negative_button) private Button cancelRelease;

    private String title;
    private Pair<String, Date> release;
    private Consumer<Pair<String, Date>> confirmationListener;

    public static ReleaseDialog create() {
        return create(null);
    }

    public static ReleaseDialog create(final Pair<String, Date> release) {
        final ReleaseDialog frag = new ReleaseDialog();
        final Bundle args = new Bundle();
        args.putSerializable(RELEASE_DATA, SerializablePair.from(release));
        frag.setArguments(args);
        return frag;
    }

    private ReleaseDialog() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_create_release, container);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoBind.bindAll(this, view);
        loadDataFromArgs();
        initUI();
        setConfirmReleaseEnabled(!getName().isEmpty());
        beginListening();
    }

    private void loadDataFromArgs() {
        final Bundle args = getArguments();
        final SerializablePair<String, Date> releaseArgument = (SerializablePair<String, Date>) args.getSerializable(RELEASE_DATA);

        if(releaseArgument == null) {
            this.release = null;
            this.title = getContext().getString(R.string.create_release);
        } else {
            this.release = releaseArgument.toPair();
            this.title = getContext().getString(R.string.modify_release);
        }
    }

    private void initUI() {
        showTitle.setText(title);

        ofNullable(editName.getEditText()).ifPresent(e -> e.setText(release == null ? "" : release.first));

        dayPicker.setMinValue(MIN_DAY);
        dayPicker.setMaxValue(MAX_DAY);

        monthPicker.setMinValue(MIN_MONTH);
        monthPicker.setMaxValue(MAX_MONTH);
        monthPicker.setDisplayedValues(Month.asStrings());

        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setWrapSelectorWheel(false);

        setInitialPickerStates();

        confirmRelease.setEnabled(!getName().isEmpty());
    }

    private void setInitialPickerStates() {
        Date initialDate = release == null ? DateUtils.now() : release.second;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(initialDate);
        dayPicker.setValue(calendar.get(Calendar.DAY_OF_MONTH));
        monthPicker.setValue(calendar.get(Calendar.MONTH));
        yearPicker.setValue(calendar.get(Calendar.YEAR));
    }

    private String getName() {
        return ofNullable(editName.getEditText())
                .map(EditText::getText)
                .map(Object::toString)
                .map(String::trim)
                .orElse("");
    }

    private void checkNameConstraints() {
        boolean emptyName = getName().isEmpty();
        if (emptyName) {
            editName.setError(getContext().getString(R.string.missing_attribute_place_of_release));
        } else {
            editName.setError(null);
        }

        setConfirmReleaseEnabled(!emptyName);
    }

    private void setConfirmReleaseEnabled(boolean enabled) {
        if(enabled) {
            confirmRelease.setTextColor(getContext().getColor(R.color.white));
        } else {
            confirmRelease.setTextColor(getContext().getColor(R.color.lighter_gray));
        }

        confirmRelease.setEnabled(enabled);
    }

    private void beginListening() {
        ofNullable(editName.getEditText()).ifPresent(e -> e.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkNameConstraints();
            }
        }));
        ofNullable(editName.getEditText())
                .ifPresent(e -> e.setOnEditorActionListener((v, actionId, event) -> {
                    if (event == null || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        closeKeyboard(getContext(), editName);
                        return true;
                    }
                    return false;
                }));

        dayPicker.setOnValueChangedListener(this);
        monthPicker.setOnValueChangedListener(this);
        yearPicker.setOnValueChangedListener(this);

        confirmRelease.setOnClickListener(this);
        cancelRelease.setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(getContext()) {
            @Override
            public boolean dispatchTouchEvent(final @NonNull MotionEvent event) {
                checkNameConstraints();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final View v = getCurrentFocus();
                    if (v instanceof EditText) {
                        Rect outRect = new Rect();
                        v.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            AndroidUtils.closeKeyboard(getContext(), v);
                        }
                    }
                }
                return super.dispatchTouchEvent(event);
            }
        };
    }

    public void setConfirmationListener(final Consumer<Pair<String, Date>> listener) {
        this.confirmationListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.positive_button) {
            if(confirmationListener != null) {
                confirmationListener.accept(paired(getName(), getDate()));
            }
            dismiss();
        } else if(view.getId() == R.id.negative_button) {
            dismiss();
        }
    }

    private Date getDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(yearPicker.getValue(), monthPicker.getValue(), dayPicker.getValue(), 0, 0, 0);
        return calendar.getTime();
    }

    @Override
    public void onValueChange(final NumberPicker picker, int oldVal, int newVal) {
        switch (picker.getId()) {
            case R.id.edit_release_day:
                break;
            case R.id.edit_release_month:
            case R.id.edit_release_year:
                updateDays();
            default:
                // do nothing
        }
    }

    private void updateDays() {
        int month = monthPicker.getValue();
        int year = yearPicker.getValue();
        int monthMaxDays = Month.values()[month].getMaxDaysWithoutLeap();
        if (month == Month.FEBRUARY.ordinal())
            monthMaxDays += DateUtils.isLeapYear(year) ? 1 : 0;
        dayPicker.setMaxValue(monthMaxDays);
    }
}
