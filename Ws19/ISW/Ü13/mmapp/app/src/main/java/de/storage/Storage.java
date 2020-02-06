package de.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.moviemanager.util.FileUtils;
import de.util.Pair;
import de.util.SerializablePair;
import de.util.Traits;
import de.util.annotations.Trait;

import static de.moviemanager.util.FileUtils.relativize;
import static de.moviemanager.util.FileUtils.resolve;
import static de.moviemanager.util.FileUtils.walk;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class Storage {
    private static final Traits TRAITS = new Traits(Storage.class);
    private static final Map<File, Storage> STORAGES = new HashMap<>();
    public static final String OBJECT_NAMES = "__object_names";

    @Trait
    private final File home;
    private final HashMap<Class<?>, StorageGroup<?>> groups;
    private final HashMap<Class<?>, Set<String>> objectNames;

    public static Storage openIn(final File home) {
        STORAGES.computeIfAbsent(home, Storage::new);
        return STORAGES.get(home);
    }

    private Storage(final File home) {
        this.home = home;
        this.groups = new HashMap<>();
        this.objectNames = new HashMap<>();

        ensureHomeExists();
        loadDataIfExists();
    }

    private void ensureHomeExists() {
        try {
            createHomeIfNotExists();
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    private void createHomeIfNotExists() throws IOException {
        if (!home.exists()) {
            FileUtils.createDirectory(home);
        }
    }

    private void loadDataIfExists() {
        File namesPath = resolve(home, OBJECT_NAMES);
        if (namesPath.exists())
            loadData();
    }

    private void loadData() {
        loadNamesForAllStoredClasses();
    }

    private void loadNamesForAllStoredClasses() {
        final File names = resolve(home, OBJECT_NAMES);
        try (final Stream<File> stream = FileUtils.list(names)) {
            stream.forEach(file -> {
                final SerializablePair<Class<?>, HashSet<String>> pair = Serial.read(file);
                objectNames.put(pair.first, pair.second);
            });
        }
    }

    public <T> void registerGroup(StorageGroup<T> group) {
        Class<?> cls = group.classOfStoredObjects();
        groups.computeIfAbsent(cls, c -> group);
        objectNames.computeIfAbsent(cls, c -> new HashSet<>());
    }

    public void write(final Object o) {
        if (groups.containsKey(o.getClass())) {
            internalWrite(o);
        } else {
            throw new StorageException("No registered group for objects of type '" + o.getClass() + "'");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void internalWrite(final T o) {
        final Class<?> cls = o.getClass();
        final StorageGroup<T> group = (StorageGroup<T>) groups.get(cls);
        final String fname = group.saveIn(home, o);
        objectNames.get(cls).add(fname);
        updateMetaData();
    }

    private void updateMetaData() {
        for (Map.Entry<Class<?>, Set<String>> entry : objectNames.entrySet()) {
            final Pair<Class<?>, HashSet<String>> pair = Pair.paired(entry).mapSecond(HashSet::new);
            final StorageGroup<?> group = groups.get(pair.first);
            final String fName = group.name() + ".ser";
            final File result = resolve(resolve(home, OBJECT_NAMES), fName);
            Serial.write(result, SerializablePair.from(pair));
        }
    }

    public List<String> getWrittenNames(final Class<?> cls) {
        final Set<String> set = objectNames.getOrDefault(cls, new HashSet<>());
        final List<String> list = new ArrayList<>(set);
        list.sort(naturalOrder());
        return unmodifiableList(list);
    }

    public <T> Optional<T> read(final Class<? extends T> cls, String name) {
        final Set<String> names = objectNames.get(cls);
        Optional<T> result = Optional.empty();
        if (names != null && names.contains(name)) {
            result = internalRead(cls, name);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> internalRead(final Class<? extends T> cls,
                                         final String name) {
        final StorageGroup<T> group = (StorageGroup<T>) groups.get(cls);
        return group.loadFrom(home, name);
    }

    public <T> void delete(T obj) {
        try {
            internalDelete(obj);
        } catch (IOException e) {
            // silent catch
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void internalDelete(final T obj) throws IOException {
        final Class<? extends T> cls = (Class<? extends T>) obj.getClass();
        final StorageGroup<T> group = (StorageGroup<T>) groups.get(cls);
        final String name = group.createFileNameFor(obj);
        objectNames.get(cls).remove(name);
        final File file = resolve(group.getDirectory(home), name);
        FileUtils.delete(file);
        updateMetaData();
    }

    public Optional<Storage> copyStorageTo(final File newHome) {
        if (spaceAlreadyOccupied(newHome)) {
            return Optional.empty();
        }

        return copyToUnoccupiedSpace(newHome);
    }

    private boolean spaceAlreadyOccupied(final File p) {
        return home.equals(p) || STORAGES.containsKey(p);
    }

    private Optional<Storage> copyToUnoccupiedSpace(final File newHome) {
        Storage copy = openIn(newHome);
        try {
            copyDataFromThisTo(copy);
            return Optional.of(copy);
        } catch (Exception e) {
            copy.deleteStorage();
            return Optional.empty();
        }
    }

    private void copyDataFromThisTo(final Storage g2) {
        copyGroups(g2);
        copyNames(g2);
        copyFiles(g2);
        g2.updateMetaData();
    }

    private void copyGroups(final Storage g2) {
        for (Map.Entry<Class<?>, StorageGroup<?>> entry : groups.entrySet()) {
            Class<?> cls = entry.getKey();
            StorageGroup<?> grp = entry.getValue();
            g2.groups.put(cls, grp);
        }
    }

    private void copyNames(final Storage g2) {
        for (Map.Entry<Class<?>, Set<String>> entry : objectNames.entrySet()) {
            Class<?> cls = entry.getKey();
            Set<String> names = entry.getValue();
            g2.objectNames.put(cls, new HashSet<>(names));
        }
    }

    private void copyFiles(final Storage g2) {
        final File srcRoot = home;
        final Predicate<File> isNotRoot = file -> !file.equals(srcRoot);

        try (final Stream<File> stream = walk(home)) {
            stream.filter(isNotRoot).forEach(src -> {
                try {
                    final File dest = resolve(g2.home, relativize(srcRoot, src));
                    FileUtils.createDirectory(dest);
                    if(!dest.isDirectory()) {
                        FileUtils.copy(src, dest);
                    }
                } catch (IOException e) {
                    throw new StorageException(e);
                }
            });
        }
    }


    public void deleteStorage() {
        deleteHome();
        groups.clear();
        objectNames.clear();
        close();
    }

    private void deleteHome() {
        try (Stream<File> stream = walk(home)) {
            stream.sorted(reverseOrder())
                    .forEach(file -> {
                        try {
                            FileUtils.delete(file);
                        } catch (IOException e) {
                            throw new StorageException(e);
                        }
                    });
        }
    }

    public void close() {
        STORAGES.remove(home);
    }

    @Override
    public String toString() {
        final String format = "Storage(home='%s', #groups=%s, #elements_per_group=%s)";
        final String homeAsString = this.home.toString();
        final String numberOfGroups = "" + groups.size();
        final String numberOfEntries = groups.entrySet()
                .stream()
                .map(Pair::paired)
                .sorted(comparing(p -> p.second.name()))
                .map(Pair::getFirst)
                .map(objectNames::get)
                .filter(Objects::nonNull)
                .map(Set::size)
                .collect(toList())
                .toString();
        return format(format, homeAsString, numberOfGroups, numberOfEntries);
    }

    @Override
    public boolean equals(Object obj) {
        return TRAITS.testEqualityBetween(this, obj);
    }

    @Override
    public int hashCode() {
        return TRAITS.createHashCodeFor(this);
    }
}
