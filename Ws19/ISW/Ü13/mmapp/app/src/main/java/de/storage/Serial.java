package de.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

enum Serial {
    ;

    public static void write(File destination, Serializable object) {
        requireNonNull(destination);
        requireNonNull(object);
        of(destination)
                .map(File::getParentFile)
                .ifPresent(File::mkdirs);
        try (ObjectOutputStream oos = createOutputStream(destination)) {
            oos.writeObject(object);
        } catch (IOException ioe) {
            throw new StorageException(ioe);
        }
    }

    private static ObjectOutputStream createOutputStream(File destionation) throws IOException {
        final FileOutputStream fos = new FileOutputStream(destionation);
        return new ObjectOutputStream(fos);
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(File source) {
        try (ObjectInputStream ois = createInputStream(source)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new StorageException(e);
        }
    }

    private static ObjectInputStream createInputStream(File source) throws IOException {
        final FileInputStream fis = new FileInputStream(source);
        return new ObjectInputStream(fis);
    }
}
