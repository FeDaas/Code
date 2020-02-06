package de.associations.shortcuts;

import java.util.function.Function;

@FunctionalInterface
public interface AssociatedObjectsCounter <U> extends Function<U, Integer> {
}
