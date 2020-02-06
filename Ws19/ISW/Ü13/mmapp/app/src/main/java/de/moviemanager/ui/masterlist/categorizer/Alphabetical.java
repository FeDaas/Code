package de.moviemanager.ui.masterlist.categorizer;

import java.util.function.Function;

import de.moviemanager.data.Nameable;
import de.moviemanager.ui.masterlist.elements.ContentElement;
import de.moviemanager.ui.masterlist.elements.DividerElement;
import de.moviemanager.ui.masterlist.elements.HeaderElement;

import static de.util.StringUtils.alphabeticalComparison;
import static java.lang.Character.toUpperCase;

public class Alphabetical<T extends Nameable> extends Categorizer<String, T> {

    private final Function<T, String> getMetaInfo;
    private final boolean useMeta;

    public Alphabetical(boolean useMeta, Function<T, String> getMetaInfo) {
        this.useMeta = useMeta;
        this.getMetaInfo = getMetaInfo;
    }

    @Override
    public String getCategoryNameFor(T obj) {
        String name = obj.name();
        if(name.isEmpty())
            return "#";
        char firstLetter = toUpperCase(name.charAt(0));
        if (Character.isAlphabetic(firstLetter))
            return "" + firstLetter;
        else
            return "#";
    }

    @Override
    public HeaderElement<T> createHeader(String category) {
        return new HeaderElement<>(category);
    }

    @Override
    protected ContentElement<T> createContent(T obj) {
        return new ContentElement<>(obj.name(), getMetaInfo.apply(obj));
    }

    @Override
    public DividerElement createDivider() {
        return new DividerElement();
    }

    @Override
    public int compareCategories(String cat1, String cat2) {
        return cat1.compareTo(cat2);
    }

    @Override
    public int compareContent(ContentElement<T> element1, ContentElement<T> element2) {
        String str1 = element1.getTitle();
        String str2 = element2.getTitle();

        if (useMeta) {
            str1 = element1.getMeta();
            str2 = element2.getMeta();
        }
        return alphabeticalComparison(str1, str2);
    }
}