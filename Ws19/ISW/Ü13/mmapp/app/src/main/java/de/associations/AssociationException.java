package de.associations;

public class AssociationException extends RuntimeException {
    AssociationException(final String msg) {
        super(msg);
    }

    public AssociationException(final Throwable t) {
        super(t);
    }

    AssociationException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
