package de.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public enum  CollectionUtils {
    ;

    public static <E> List<List<E>> split(final List<E> list, final E border) {
        final List<List<E>> result = new ArrayList<>();
        List<E> block = new ArrayList<>();

        for(final E element : list) {
            if(element.equals(border)) {
                result.add(block);
                block = new ArrayList<>();
            } else {
                block.add(element);
            }
        }
        if(!block.isEmpty()) {
            result.add(block);
        }
        return result;
    }

    public static <E, T> List<T> map(final Function<E, T> mapper, final List<E> li) {
        return li.stream().map(mapper).collect(toList());
    }
}
