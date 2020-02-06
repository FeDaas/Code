package de.associations.shortcuts;

import java.util.function.Function;

@FunctionalInterface
public interface IdMapper<T> extends Function<T, Integer> {

}
