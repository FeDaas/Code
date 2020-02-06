package de.moviemanager.ui.adapter.selection;

import android.content.Context;

import androidx.annotation.LayoutRes;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import de.moviemanager.R;
import de.moviemanager.ui.adapter.base.ContentBinder;

import static de.util.ObjectUtils.requireAllNonNull;

public class SelectionAdapterBuilder<T> {
    private final Context context;
    @LayoutRes private int wrapperLayoutId;
    @LayoutRes private int elementLayoutId;
    private List<T> data;

    private BiPredicate<T, CharSequence> filterCriterion;
    private Comparator<T> orderCriterion;
    private ContentBinder<T> binder;

    private Consumer<SelectionChangedContext<T>> onSelectionChanged;

    SelectionAdapterBuilder(final Context context) {
        this.context = context;
        useCheckBox();
    }

    SelectionAdapterBuilder<T> useCheckBox() {
        wrapperLayoutId = R.layout.listitem_selectable;
        return this;
    }

    SelectionAdapterBuilder<T> useRadioButton() {
        wrapperLayoutId = R.layout.listitem_one_selectable;
        return this;
    }

    public SelectionAdapterBuilder<T> setElementLayoutId(@LayoutRes int elementLayoutId) {
        this.elementLayoutId = elementLayoutId;
        return this;
    }

    public SelectionAdapterBuilder<T> setData(final List<T> data) {
        this.data = data;
        return this;
    }

    public SelectionAdapterBuilder<T> setFilterCriterion(final BiPredicate<T, CharSequence> filterCriterion) {
        this.filterCriterion = filterCriterion;
        return this;
    }

    public SelectionAdapterBuilder<T> setOrderCriterion(final Comparator<T> orderCriterion) {
        this.orderCriterion = orderCriterion;
        return this;
    }

    public SelectionAdapterBuilder<T> setContentBinder(final ContentBinder<T> binder) {
        this.binder = binder;
        return this;
    }

    SelectionAdapterBuilder<T> setOnSelectionChanged(
            final Consumer<SelectionChangedContext<T>> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
        return this;
    }

    public SelectionAdapter<T> build() {
        requireAllNonNull(
                context,
                data,
                filterCriterion,
                orderCriterion,
                binder
        );

        final SelectionAdapter<T> result = new SelectionAdapter<T>(context,
                elementLayoutId,
                data,
                filterCriterion,
                orderCriterion,
                binder) {
            @Override
            protected void onSelectionChanged(T element, boolean isSelected) {
                final SelectionChangedContext<T> changedContext = new SelectionChangedContext<>(
                        this,
                        data,
                        element,
                        isSelected
                );
                onSelectionChanged.accept(changedContext);
            }
        };
        result.setWrapperLayoutId(wrapperLayoutId);
        return result;
    }
}
