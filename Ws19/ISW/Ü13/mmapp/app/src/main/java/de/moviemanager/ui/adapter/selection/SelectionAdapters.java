package de.moviemanager.ui.adapter.selection;

import android.content.Context;

import java.util.function.Consumer;

public enum SelectionAdapters {
    ;

    public static <T> SelectionAdapterBuilder<T> singleSelection(
            final Context context,
            final SingleSelectionListener<T> listener
    ) {
        return new SelectionAdapterBuilder<T>(context)
                .useRadioButton()
                .setOnSelectionChanged(singleChanged(listener));
    }

    private static <T> Consumer<SelectionChangedContext<T>> singleChanged(final SingleSelectionListener<T> listener) {
        return new Consumer<SelectionChangedContext<T>>() {
            private T lastSelected;

            @Override
            public void accept(final SelectionChangedContext<T> changedContext) {
                final T element = changedContext.element;
                changedContext.data.forEach(SelectionProxy::disable);

                if(element != null && !element.equals(lastSelected)) {
                    lastSelected = element;
                    listener.currentSelected(element);
                } else {
                    listener.currentSelected(null);
                }
                changedContext.adapter.notifyDataSetChanged();
            }
        };
    }

    public static <T> SelectionAdapterBuilder<T> multiSelection(
            final Context context,
            final MultiSelectionListener<T> listener) {
        return new SelectionAdapterBuilder<T>(context)
                .useCheckBox()
                .setOnSelectionChanged(changedContext -> {
                    final T element = changedContext.element;
                    if (changedContext.isSelected) {
                        listener.onElementSelected(element);
                    } else {
                        listener.onElementUnselected(element);
                    }
                });
    }
}
