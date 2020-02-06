package de.associations.shortcuts;

import java.util.function.Function;

@FunctionalInterface
public interface IdUnmapper <T> extends Function<Integer, T> {
}
