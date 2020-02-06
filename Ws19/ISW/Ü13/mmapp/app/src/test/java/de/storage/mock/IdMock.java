package de.storage.mock;

import de.util.Identifiable;

import static java.util.Objects.requireNonNull;

public class IdMock implements Identifiable {
    private final int IDENTIFIER;
    private String attr;

    public IdMock(int id) {
        this.IDENTIFIER = id;
        this.attr = "";
    }

    public void changeAttribute(String attr) {
        this.attr = requireNonNull(attr);
    }

    public String getAttribute() {
        return attr;
    }

    @Override
    public int id() {
        return IDENTIFIER;
    }
}
