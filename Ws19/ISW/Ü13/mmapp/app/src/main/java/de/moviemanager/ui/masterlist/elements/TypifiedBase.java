package de.moviemanager.ui.masterlist.elements;

import static de.moviemanager.ui.masterlist.elements.Type.CONTENT;
import static de.moviemanager.ui.masterlist.elements.Type.DIVIDER;
import static de.moviemanager.ui.masterlist.elements.Type.HEADER;

public class TypifiedBase implements Typified {
    private final Type type;

    public TypifiedBase(Type type) {
        this.type = type;
    }

    @Override
    public int getTypeAsInt() {
        return type.ordinal();
    }

    @Override
    public boolean isHeader() {
        return type == HEADER;
    }

    @Override
    public boolean isContent() {
        return type == CONTENT;
    }

    @Override
    public boolean isDivider() {
        return type == DIVIDER;
    }

    @Override
    public boolean hasSameTypeAs(Typified e) {
        return getTypeAsInt() == e.getTypeAsInt();
    }

    @Override
    public String getTypeAsString() {
        return type.name();
    }
}
