package de.moviemanager.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.ui.dialog.DateSelectionDialog;

import static de.util.DateUtils.dateToText;
import static de.util.DateUtils.textToDate;

public class DateSelectionView extends FrameLayout {
    private TextView showDate;
    private ImageView selectDate;
    private ImageView removeDate;
    private TextView showError;

    private boolean errorEnable = true;
    private boolean editEnable = true;
    private String errorText = "";
    private String dateFormat = "dd.MM.yyyy";
    private SimpleDateFormat formatter;
    private Date date;
    private Date minDate;
    private Date maxDate;

    private BiConsumer<Date, Date> selectListener = (oldDate, newDate) -> {};
    private Consumer<Date> removeListener = oldDate -> {};

    public DateSelectionView(final Context context) {
        this(context,null);
    }

    public DateSelectionView(final Context context,
                             final AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DateSelectionView(final Context context,
                             final AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

        if(date == null)
        {
            hideDateSettings();
        }

    }

    private void init(final AttributeSet attrs) {
        initFromAttributes(attrs);
        if(editEnable)
        {
            inflate(getContext(), R.layout.view_date_selection, this);
            bindEditViews();
            initEditViewContent();
        } else {
            inflate(getContext(), R.layout.view_date_selection_nonedit, this);
            bindViews();
            initViewContent();
        }
    }

    private void initFromAttributes(final AttributeSet attrs) {
        if(attrs == null)
            return;

        final TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DateSelectionView);
        errorEnable = ta.getBoolean(R.styleable.DateSelectionView_errorEnable, errorEnable);
        editEnable = ta.getBoolean(R.styleable.DateSelectionView_editEnable, editEnable);
        errorText = getString(ta, R.styleable.DateSelectionView_errorText, errorText);
        dateFormat = getString(ta, R.styleable.DateSelectionView_dateFormat, dateFormat);

        formatter = new SimpleDateFormat(dateFormat, Locale.US);

        date = textToDate(formatter, getString(ta, R.styleable.DateSelectionView_date, ""));
        minDate = textToDate(formatter, getString(ta, R.styleable.DateSelectionView_minDate, ""));
        maxDate = textToDate(formatter, getString(ta, R.styleable.DateSelectionView_maxDate, ""));

        ta.recycle();
    }

    private String getString(final TypedArray ta, int id, final String def) {
        String s = ta.getString(id);
        return s == null ? def : s;
    }

    private void bindEditViews() {
        showDate = findViewById(R.id.show_date);
        selectDate = findViewById(R.id.select_date);
        removeDate = findViewById(R.id.remove_date);
        showError = findViewById(R.id.show_error);
    }

    private void bindViews() {
        showDate = findViewById(R.id.show_date);
        showError = findViewById(R.id.show_error);
    }

    private void initEditViewContent() {
        initShowError();

        selectDate.setOnClickListener(this::onDateSelection);
        removeDate.setOnClickListener(this::onDateRemoval);

        showDate.setText(dateToText(formatter, date));
        setRemoveDateEnabled(date != null);
        hideDateSettings();
    }

    public void hideDateSettings()
    {
        if(this.getId() == R.id.edit_due_date)
        {
            View daddy = (View)this.getParent();
            if(daddy != null)
            {
                DateSelectionView son = daddy.findViewById(R.id.edit_watch_date);
                son.selectDate.setVisibility(View.INVISIBLE);
                son.removeDate.setVisibility(View.INVISIBLE);
                //this.setVisibility(View.GONE);
            }
        }
    }

    public void showDateSettings()
    {
        if(this.getId() == R.id.edit_due_date)
        {
            View daddy = (View)this.getParent();
            if(daddy != null)
            {
                DateSelectionView son = daddy.findViewById(R.id.edit_watch_date);
                son.selectDate.setVisibility(View.VISIBLE);
                son.removeDate.setVisibility(View.VISIBLE);
                //this.setVisibility(View.GONE);
            }
        }
    }

    private void initShowError() {
        if(!errorEnable) {
            findViewById(R.id.date_end).setVisibility(GONE);
            showError.setVisibility(GONE);
        } else {
            showError.setText(errorText);
        }
    }

    private void onDateSelection(final View view) {
        if(view.getId() != selectDate.getId()) {
            return;
        }
        final FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
        final DateSelectionDialog dialog = DateSelectionDialog.create(date, minDate, maxDate);
        dialog.setDateChangeListener(newDate -> {
            if(newDate == null) {
                return;
            }

            setRemoveDateEnabled(true);
            selectListener.accept(date, newDate);
            date = newDate;
            showDate.setText(dateToText(formatter, date));
            hideDateSettings();
        });
        dialog.show(fm, "dialog_date_selection");
    }

    private void setRemoveDateEnabled(boolean enabled) {
        removeDate.setEnabled(enabled);
        if(enabled) {
            removeDate.setColorFilter(getContext().getColor(R.color.dark_red));
        } else {
            removeDate.setColorFilter(getContext().getColor(R.color.light_gray));
        }
    }

    private void onDateRemoval(final View view) {
        if(view.getId() != removeDate.getId()) {
            return;
        }

        setRemoveDateEnabled(false);
        removeListener.accept(date);

        date = null;
        showDate.setText(dateToText(formatter, null));
        showDateSettings();

    }

    private void initViewContent() {
        initShowError();

        showDate.setText(dateToText(formatter, date));

    }

    public void setDate(final Date date) {
        this.date = date;
        if(removeDate != null) {
            setRemoveDateEnabled(date != null);
        }
        showDate.setText(dateToText(formatter, date));
        invalidate();
    }

    public Date getDate()
     {
        return this.date;
    }

    private void setRemoveListener(final Consumer<Date> removeListener) {
        this.removeListener = removeListener;
    }

    private void setSelectListener(final BiConsumer<Date, Date> selectListener) {
        this.selectListener = selectListener;
    }

    public void setDateChangeListener(final Consumer<Date> listener) {
        setRemoveListener(listener);
        setSelectListener((oldDate, newDate) -> listener.accept(oldDate));
    }

    public void setErrorText(String msg) {
        showError.setText(msg);
    }
}


