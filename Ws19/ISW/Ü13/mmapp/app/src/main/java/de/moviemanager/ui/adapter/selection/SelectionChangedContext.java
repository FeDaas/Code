package de.moviemanager.ui.adapter.selection;

import java.util.List;

public class SelectionChangedContext<E> {
    public final SelectionAdapter<E> adapter;
    public final List<SelectionProxy<E>> data;
    public final E element;
    public final boolean isSelected;

    SelectionChangedContext(SelectionAdapter<E> adapter,
                            List<SelectionProxy<E>> data,
                            E element,
                            boolean isSelected) {
        this.adapter = adapter;
        this.data = data;
        this.element = element;
        this.isSelected = isSelected;
    }
}