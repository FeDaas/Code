package de.moviemanager.ui.masterlist.elements;

public interface Typified {
    int getTypeAsInt();
    boolean isHeader();
    boolean isContent();
    boolean isDivider();
    boolean hasSameTypeAs(Typified e);
    String getTypeAsString();
}
