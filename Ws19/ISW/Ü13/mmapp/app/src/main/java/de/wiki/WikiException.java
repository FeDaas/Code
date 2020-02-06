package de.wiki;

public class WikiException extends RuntimeException {
    public WikiException(final String msg) {
        super(msg);
    }

    public WikiException(final Throwable t) {
        super(t);
    }

    public WikiException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
