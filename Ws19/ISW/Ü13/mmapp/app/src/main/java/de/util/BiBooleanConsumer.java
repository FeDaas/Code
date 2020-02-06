package de.util;

@FunctionalInterface
public interface BiBooleanConsumer<E> {
    void accept(E elem, boolean b);
}
