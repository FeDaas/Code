package de.storage;

public class StorageException extends RuntimeException {
    public StorageException(final String msg) {
        super(msg);
    }

    public StorageException(final Throwable t) {
        super(t);
    }
}
