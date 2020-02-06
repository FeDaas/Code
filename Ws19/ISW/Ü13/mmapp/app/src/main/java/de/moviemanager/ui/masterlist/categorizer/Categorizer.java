package de.moviemanager.ui.masterlist.categorizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.DividerElement;
import de.moviemanager.ui.masterlist.elements.Element;
import de.moviemanager.ui.masterlist.elements.HeaderElement;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public abstract class Categorizer<C, T> {

    protected abstract C getCategoryNameFor(T obj);

    protected abstract HeaderElement<T> createHeader(C category);

    private ContentElement<T> createAttachedContent(T obj) {
        ContentElement<T> result = createContent(obj);
        result.attachContentModel(obj);
        return result;
    }

    protected abstract ContentElement<T> createContent(T obj);

    protected abstract DividerElement createDivider();

    protected abstract int compareCategories(C cat1, C cat2);

    protected abstract int compareContent(ContentElement<T> comp1, ContentElement<T> comp2);

    public List<Element> createToCategorizedList(List<T> objects, boolean descending) {
        Map<C, List<T>> groups =  groupInput(objects);
        List<C> categories = groups.keySet()
                .stream()
                .sorted(this::compareCategories)
                .collect(toList());
        if(descending)
            categories = flipOrder(categories);

        List<Element> result = new ArrayList<>();
        for(C category : categories) {
            HeaderElement<T> header = createHeader(category);
            result.add(header);
            List<ContentElement<T>> contentOfCategory = ofNullable(groups.get(category))
                    .orElse(emptyList())
                    .stream()
                    .map(this::createAttachedContent)
                    .sorted(this::compareContent)
                    .collect(toList());

            if(descending)
                contentOfCategory = flipOrder(contentOfCategory);
            result.addAll(contentOfCategory);

            DividerElement divider = createDivider();
            result.add(divider);

            contentOfCategory.forEach(header::addContentElement);
            header.assignDivider(divider);
        }

        return result;
    }

    private <X> List<X> flipOrder(List<X> li) {
        return range(0, li.size())
                .map(i -> li.size() - 1 - i)
                .mapToObj(li::get)
                .collect(toList());
    }

    private Map<C, List<T>> groupInput(List<T> objects) {
        return objects.stream().collect(groupingBy(this::getCategoryNameFor));
    }

}
