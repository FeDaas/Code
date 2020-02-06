package de.moviemanager.ui.adapter.base;

@FunctionalInterface
public interface IndexedAdapter<T> {
    T getElementByPosition(int position);
}
