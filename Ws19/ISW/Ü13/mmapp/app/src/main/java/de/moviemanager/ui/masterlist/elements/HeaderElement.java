package de.moviemanager.ui.masterlist.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static de.moviemanager.ui.masterlist.elements.Type.HEADER;

public class HeaderElement<T> extends Element {
    private final List<ContentElement<T>> contents;
    private DividerElement divider;

    public HeaderElement(String header) {
        super(HEADER, header);
        contents = new ArrayList<>();
        divider = null;
    }

    public void addContentElement(ContentElement<T> elem) {
        this.contents.add(elem);
        elem.setHeader(this);
    }

    public void removeContentElement(ContentElement<T> elem) {
        this.contents.remove(elem);
    }

    public void assignDivider(DividerElement divider) {
        this.divider = divider;
    }

    public String getHeader() {
        return getContent();
    }

    public void resetFilter() {
        filter(x -> true);
    }

    public void filter(Predicate<T> constraint) {
        contents.forEach(content -> {
            T model = content.retrieveContentModel();
            boolean visible = constraint.test(model);
            content.setVisible(visible);
        });
        boolean visible = contents.stream().anyMatch(ContentElement::isVisible);
        divider.setVisible(visible);
        setVisible(visible);
    }
}