package de.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.moviemanager.util.FileUtils;

import static de.storage.Storage.OBJECT_NAMES;
import static de.moviemanager.util.FileUtils.exists;
import static java.nio.file.Files.list;
import static java.nio.file.Files.walk;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageTest {
    private static final File STORAGE_PATH = new File("__testStorage");
    private static final File OTHER_STORAGE_PATH = new File("__otherTestStorage");
    private static final File EMPTY_STORAGE_PATH = new File("__emptyTestStorage");
    private Storage testStorage;
    private Storage otherStorage;
    private int[] integerSampleData;
    private String[] stringSampleData;

    @BeforeEach
    void init() {
        testStorage = Storage.openIn(STORAGE_PATH);
        otherStorage = Storage.openIn(OTHER_STORAGE_PATH);
        integerSampleData = new int[]{5, 99, 0, 60, -3, 11};
        stringSampleData = "hello darkness my old friend".split(" ");

        assertFalse(exists(EMPTY_STORAGE_PATH));
    }

    @AfterEach
    void tearDown() {
        testStorage.deleteStorage();
        otherStorage.deleteStorage();
    }

    @Test
    void testWriteWithOneGroup() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        final File p5 = FileUtils.resolve(STORAGE_PATH, FileUtils.get("integers", "i_5.ser"));
        final File p10 = FileUtils.resolve(STORAGE_PATH, FileUtils.get("integers", "i_10.ser"));

        // precondition
        assertFalse(exists(p5));
        assertFalse(exists(p10));

        // test
        testStorage.write(5);
        testStorage.write(10);
        assertTrue(exists(p5));
        assertTrue(exists(p10));
    }

    @Test
    void testDeleteWithOneGroup() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        File p10 = FileUtils.resolve(STORAGE_PATH, FileUtils.get("integers", "i_10.ser"));
        testStorage.write(10);

        // precondition
        assertTrue(exists(p10));

        // test
        testStorage.delete(10);
        assertFalse(exists(p10));
    }

    @Test
    void testReadWithOneGroup() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        final File p5 = FileUtils.resolve(STORAGE_PATH, FileUtils.get("integers", "i_5.ser"));
        testStorage.write(5);

        // precondition
        assertTrue(exists(p5));

        // test
        final Optional<Integer> opt = testStorage.read(Integer.class, "i_5.ser");
        assertTrue(opt.isPresent());
        assertEquals(5, opt.get().intValue());
    }

    @Test
    void testWithoutRegisteredGroup() {
        // setup
        final Integer obj = 5;
        final Executable r = () -> testStorage.write(obj);
        final String expectedMessage = "No registered group for objects of type '" + obj.getClass() + "'";

        // test
        assertThrows(RuntimeException.class, r, expectedMessage);
    }

    @Test
    void testReloadingOfStorage() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        range(0, 42).forEach(testStorage::write);
        final List<String> names = testStorage.getWrittenNames(Integer.class);
        final Map<String, Integer> namedIntegers = new HashMap<>();

        // precondition
        names.forEach(name -> {
            Optional<Integer> opt = testStorage.read(Integer.class, name);
            assertTrue(opt.isPresent());
            namedIntegers.put(name, opt.get());
        });
        testStorage.close();

        // test
        testStorage = Storage.openIn(STORAGE_PATH);
        testStorage.registerGroup(new IntegerGroup());
        final List<String> reloadedNames = testStorage.getWrittenNames(Integer.class);
        assertEquals(names, reloadedNames);
        for (String name : reloadedNames) {
            Integer object = namedIntegers.get(name);
            Integer reloadedObject = namedIntegers.get(name);
            assertEquals(object, reloadedObject);
        }
    }

    @Test
    void testStorageOpenAndDeletionWithoutStoredData() throws IOException {
        // setup
        final Storage storage = Storage.openIn(EMPTY_STORAGE_PATH);
        assertTrue(exists(EMPTY_STORAGE_PATH));
        assertExpectedFileCountInSubdirOfEmpty(0, "");

        // test
        storage.deleteStorage();
        assertFalse(exists(EMPTY_STORAGE_PATH));
    }

    @Test
    void testCreationAndDeletionWithStoredData() throws IOException {
        // setup
        final Storage storage = Storage.openIn(EMPTY_STORAGE_PATH);
        storage.registerGroup(new IntegerGroup());
        storage.registerGroup(new StringGroup());

        // precondition
        assertTrue(exists(EMPTY_STORAGE_PATH));
        assertExpectedFileCountInSubdirOfEmpty(0, "");

        // test
        asList(5, 99, 0).forEach(storage::write);
        stream(stringSampleData).forEach(storage::write);

        assertExpectedTotalFileCountOfEmpty(13, "");
        assertExpectedFileCountInSubdirOfEmpty(3, "");
        assertExpectedFileCountInSubdirOfEmpty(2, OBJECT_NAMES);
        assertExpectedFileCountInSubdirOfEmpty(3, "integers");
        assertExpectedFileCountInSubdirOfEmpty(5, "strings");

        storage.deleteStorage();
        assertFalse(exists(EMPTY_STORAGE_PATH));
    }

    private void assertExpectedTotalFileCountOfEmpty(long expected, String dir) throws IOException {
        final File file = FileUtils.resolve(EMPTY_STORAGE_PATH, dir);
        try (Stream<Path> stream = walk(file.toPath())) {
            assertEquals(expected, stream.count() - 1);
        }
    }

    private void assertExpectedFileCountInSubdirOfEmpty(long expected, String dir) throws IOException {
        final File file = FileUtils.resolve(EMPTY_STORAGE_PATH, dir);
        try (Stream<Path> stream = list(file.toPath())) {
            assertEquals(expected, stream.count());
        }
    }

    @Test
    void testCopyStorage() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        testStorage.registerGroup(new StringGroup());
        stream(integerSampleData).forEach(testStorage::write);
        stream(stringSampleData).forEach(testStorage::write);
        File copyPath = new File(STORAGE_PATH + "_copy");

        // test
        Optional<Storage> copiedStorageOpt = testStorage.copyStorageTo(copyPath);
        assertTrue(copiedStorageOpt.isPresent());
        Storage copiedStorage = copiedStorageOpt.get();
        assertNotEquals(testStorage, copiedStorage);

        checkWrittenObjects(copiedStorage, Integer.class);
        checkWrittenObjects(copiedStorage, String.class);

        copiedStorage.deleteStorage();
        assertFalse(exists(copyPath));
    }

    private void checkWrittenObjects(Storage copiedStorage, Class<?> cls) {
        for (String name : testStorage.getWrittenNames(cls)) {
            final Optional<?> orginalObject = testStorage.read(cls, name);
            final Optional<?> copiedObject = copiedStorage.read(cls, name);
            assertEquals(orginalObject, copiedObject);
            assertTrue(testStorage.read(cls, name).isPresent());

            copiedStorage.delete(copiedObject.get());
            final Optional<?>  originalObjectReloaded = testStorage.read(cls, name);
            final Optional<?>  copiedObjectReloaded = copiedStorage.read(cls, name);
            assertTrue(originalObjectReloaded.isPresent());
            assertFalse(copiedObjectReloaded.isPresent());
            assertNotEquals(originalObjectReloaded, copiedObjectReloaded);
        }
    }

    @Test
    void testToStringWithEmptyStorage() {
        // setup
        String expectedEmpty = createStringForEmptyStorage();

        // test
        assertEquals(expectedEmpty, testStorage.toString());
    }

    @Test
    void testToString() {
        // setup
        testStorage.registerGroup(new IntegerGroup());
        testStorage.registerGroup(new StringGroup());

        stream(integerSampleData).forEach(testStorage::write);
        stream(stringSampleData).forEach(testStorage::write);
        int[] sizes = new int[]{integerSampleData.length, stringSampleData.length};

        // test
        String expectedFilled = createStringForGroupSizes(sizes);
        assertEquals(expectedFilled, testStorage.toString());
    }

    private String createStringForEmptyStorage() {
        return "Storage" +
                "(" +
                "home=" + "'" + STORAGE_PATH + "'" +
                ", #groups=" + 0 +
                ", #elements_per_group=[]" +
                ")";
    }

    private String createStringForGroupSizes(int[] sizes) {
        final List<Integer> sizesAsList = stream(sizes).boxed().collect(toList());
        return "Storage" +
                "(" +
                "home=" + "'" + STORAGE_PATH + "'" +
                ", #groups=" + sizes.length +
                ", #elements_per_group=" + sizesAsList +
                ")";
    }

    @Test
    void testEquals() {
        // test
        assertNotEquals(null, testStorage);
        assertNotEquals("potato", testStorage);
        assertEquals(testStorage, testStorage);

        assertSame(testStorage, Storage.openIn(STORAGE_PATH));
        assertNotEquals(testStorage, otherStorage);
    }

    @Test
    void testHashCode() {
        // test
        assertEquals(testStorage.hashCode(), testStorage.hashCode());
        assertNotEquals(testStorage.hashCode(), otherStorage.hashCode());
        assertEquals(testStorage.hashCode(), Storage.openIn(STORAGE_PATH).hashCode());
    }

    @Test
    void testCopyToExistingHome() {
        // setup
        Optional<Storage> copyOpt1 = testStorage.copyStorageTo(STORAGE_PATH);
        Optional<Storage> copyOpt2 = testStorage.copyStorageTo(OTHER_STORAGE_PATH);

        // test
        assertFalse(copyOpt1.isPresent());
        assertFalse(copyOpt2.isPresent());
    }

    @Test
    void testDeleteIfFileWasDeletedManually() throws IOException {
        // setup
        IntegerGroup grp = new IntegerGroup();
        int obj = -69237;
        File dir = FileUtils.resolve(STORAGE_PATH, "integers");
        File file = FileUtils.resolve(dir, grp.createFileNameFor(obj));
        testStorage.registerGroup(grp);
        testStorage.write(obj);
        FileUtils.delete(file);

        // test
        testStorage.delete(obj);
        assertFalse(testStorage.read(Integer.class, "i_-69237.ser").isPresent());
    }
}

abstract class SerializableGroup<T extends Serializable> extends StorageGroup<T> {

    SerializableGroup(final Class<T> cls, final String name) {
        super(cls, name);
    }

    @Override
    protected void saveToStorage(final File file, final T obj) {
        Serial.write(file, obj);
    }

    @Override
    protected Optional<T> loadFromStorage(final File dir, final String objName) {
        try {
            return Optional.of(Serial.read(FileUtils.resolve(dir, objName)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

class IntegerGroup extends SerializableGroup<Integer> {

    IntegerGroup() {
        super(Integer.class, "integers");
    }

    @Override
    public String createFileNameFor(Integer obj) {
        return "i_" + obj + ".ser";
    }
}

class StringGroup extends SerializableGroup<String> {
    StringGroup() {
        super(String.class, "strings");
    }

    @Override
    public String createFileNameFor(String obj) {
        return "s_" + obj.hashCode() + ".ser";
    }
}
