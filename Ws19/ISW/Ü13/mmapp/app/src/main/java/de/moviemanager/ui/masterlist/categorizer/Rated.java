package de.moviemanager.ui.masterlist.categorizer;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;

import de.moviemanager.data.Nameable;
import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.DividerElement;
import de.moviemanager.ui.masterlist.elements.HeaderElement;
import de.moviemanager.util.RatingUtils;
import de.util.StringUtils;

import static de.moviemanager.util.RatingUtils.ratingToString;
import static java.util.Comparator.comparing;

public class Rated<T extends Nameable> extends Categorizer<String, T> {
    private final Comparator<ContentElement<T>> contentComparator =
            comparing(ContentElement<T>::getMeta)
                    .thenComparing(ContentElement::getTitle, StringUtils::alphabeticalComparison);

    private final String numberName;
    private final ToDoubleFunction<T> getNumber;

    public Rated(final String numberName, final ToDoubleFunction<T> getNumber) {
        this.numberName = numberName;
        this.getNumber = getNumber;
    }

    @Override
    public String getCategoryNameFor(final T obj) {
        int rating = (int) (getNumber.applyAsDouble(obj));
        return RatingUtils.ratingToString(rating);
    }

    @Override
    public HeaderElement<T> createHeader(final String category) {
        return new HeaderElement<>(category);
    }

    @Override
    protected ContentElement<T> createContent(T obj) {
        double rating = getNumber.applyAsDouble(obj);
        String sub = ratingToString(rating, numberName.contains("Overall"));
        return new ContentElement<>(obj.name(), sub);
    }

    @Override
    public DividerElement createDivider() {
        return new DividerElement();
    }

    @Override
    public int compareCategories(String cat1, String cat2) {
        int rank1 = textRatingToString(cat1.trim());
        int rank2 = textRatingToString(cat2.trim());
        return Integer.compare(rank1, rank2);
    }

    private int textRatingToString(final String s) {
        int result;
        if("Not Rated".equals(s)){
            result = -1;
        } else {
            result = s.replace("☆", "").length();
        }
        return result;
    }

    @Override
    public int compareContent(final ContentElement<T> element1, final ContentElement<T> element2) {
        return contentComparator.compare(element1, element2);
    }
}