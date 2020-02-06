package de.storage;

import java.io.File;
import java.util.Optional;

import de.moviemanager.data.Nameable;
import de.moviemanager.util.FileUtils;

import static java.util.Optional.ofNullable;

public abstract class StorageGroup<T> implements Nameable {
    private final Class<T> cls;
    private final String name;

    protected StorageGroup(Class<T> cls, String name) {
        this.cls = cls;
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    public Class<T> classOfStoredObjects() {
        return cls;
    }

    public String saveIn(final File home, T o) {
        final File directory = getDirectory(home);
        String fname = createFileNameFor(o);
        final File filePath = FileUtils.resolve(directory, fname);
        ofNullable(filePath)
                .map(File::getParentFile)
                .ifPresent(File::mkdirs);
        saveToStorage(filePath, o);
        return fname;
    }

    public File getDirectory(final File home) {
        return FileUtils.resolve(home, name);
    }

    public abstract String createFileNameFor(T obj);

    protected abstract void saveToStorage(File directory, T obj);

    public Optional<T> loadFrom(final File home, String objName) {
        File directory = getDirectory(home);
        return loadFromStorage(directory, objName);
    }

    protected abstract Optional<T> loadFromStorage(File dir, String objName);
}
