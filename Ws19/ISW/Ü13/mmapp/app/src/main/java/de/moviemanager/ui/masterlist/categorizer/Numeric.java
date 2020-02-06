package de.moviemanager.ui.masterlist.categorizer;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import de.moviemanager.data.Nameable;
import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.DividerElement;
import de.moviemanager.ui.masterlist.elements.HeaderElement;
import de.util.StringUtils;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

public class Numeric<T extends Nameable> extends Categorizer<String, T> {
    private final Comparator<ContentElement<T>> contentComparator =
            comparing(ContentElement<T>::getMeta)
            .thenComparing(ContentElement::getTitle, StringUtils::alphabeticalComparison);

    private final String numberName;
    private final ToDoubleFunction<T> getNumber;
    private final double step;

    public Numeric(final String numberName, double step, final ToDoubleFunction<T> getNumber) {
        this.numberName = numberName;
        this.step = step;
        this.getNumber = getNumber;
    }

    @Override
    public String getCategoryNameFor(final T obj) {
        int number = (int) (100 * getNumber.applyAsDouble(obj));
        int scaledStep = (int) (100 * step);
        int normedNumber = scaledStep * (number / scaledStep);
        return format(Locale.GERMAN, "%3d", normedNumber / 100);
    }

    @Override
    public HeaderElement<T> createHeader(final String category) {
        return new HeaderElement<>(category);
    }

    @Override
    protected ContentElement<T> createContent(final T obj) {
        String name = obj.name();
        double number = getNumber.applyAsDouble(obj);

        return new ContentElement<>(name, numberName + ": " + number);
    }

    @Override
    public DividerElement createDivider() {
        return new DividerElement();
    }

    @Override
    public int compareCategories(final String cat1, final String cat2) {
        int rank1 = Integer.parseInt(cat1.trim());
        int rank2 = Integer.parseInt(cat2.trim());
        return Integer.compare(rank1, rank2);
    }

    @Override
    public int compareContent(final ContentElement<T> element1, final ContentElement<T> element2) {
        return contentComparator.compare(element1, element2);
    }
}

