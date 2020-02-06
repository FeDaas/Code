package de.associations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.util.Pair;

import static de.util.Pair.paired;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiMapTest {
    private final BiMap<Integer, String> map;
    private final BiMap<Integer, String> copiedMap;
    private final BiMap<String, Boolean> otherMap;
    private final List<Pair<Integer, Integer>> mapIds;

    BiMapTest() {
        mapIds = new ArrayList<>();
        map = new BiMap<>();
        copiedMap = new BiMap<>();
        otherMap = new BiMap<>();
    }

    @BeforeEach
    void setup() {
        range(0, 10).forEach(i -> {
            map.add(i, getEvenOrOddText(i));
            copiedMap.add(i, getEvenOrOddText(i));
        });
        for (int i = 0; i < 10; ++i) {
            mapIds.add(paired(i, i % 2));
        }
    }

    private String getEvenOrOddText(int i) {
        String result = "odd";
        if (i % 2 == 0) {
            result = "even";
        }
        return result;
    }

    private int getIdForCategories(final String cat) {
        int result = 1;
        if ("even".equals(cat)) {
            result = 0;
        }
        return result;
    }

    @AfterEach
    void tearDown() {
        mapIds.clear();
        map.clear();
        copiedMap.clear();
    }

    @Test
    void testAddEntries() {
        // precondition
        for (int i = 0; i < 10; ++i) {
            final Optional<List<String>> columnOpt = map.getColumn(i);
            assertTrue(columnOpt.isPresent());

            final List<String> column = columnOpt.get();
            assertEquals(1, column.size());
            assertEquals(getEvenOrOddText(i), column.get(0));
        }

        // test
        final Optional<List<Integer>> evenRowOpt = map.getRow("even");
        final Optional<List<Integer>> oddRowOpt = map.getRow("odd");
        assertTrue(evenRowOpt.isPresent());
        assertTrue(oddRowOpt.isPresent());

        long numberEven = evenRowOpt.get()
                .stream()
                .filter(this::isEven)
                .count();
        long numberOdd = oddRowOpt.get()
                .stream()
                .filter(this::isOdd)
                .count();

        assertEquals(5, evenRowOpt.get().size());
        assertEquals(5, evenRowOpt.get().size());
        assertEquals(5, numberEven);
        assertEquals(5, numberOdd);
    }

    private boolean isEven(int i) {
        return i % 2 == 0;
    }

    private boolean isOdd(int i) {
        return i % 2 == 1;
    }

    @Test
    void testIdPairCreation() {
        // setup
        final List<Pair<Integer, Integer>> ids = new ArrayList<>();

        // precondition
        assertNotEquals(mapIds, ids);

        // test
        ids.addAll(map.getIdPairs(i -> i, this::getIdForCategories));
        assertEquals(mapIds, ids);
    }

    @Test
    void testEquals() {
        // test
        assertNotEquals(null, map);
        assertNotEquals("potatoe", map);
        assertEquals(map, map);
        assertEquals(map, copiedMap);
        assertNotEquals(map, otherMap);
    }

    @Test
    void testHashcode() {
        // test
        assertEquals(map.hashCode(), map.hashCode());
        assertEquals(map.hashCode(), copiedMap.hashCode());
        assertNotEquals(map.hashCode(), otherMap.hashCode());
    }

    @Test
    void testRemove() {
        // precondition
        assertEquals(5, map.sizeOfNonEmptyColumns("even"));
        assertEquals(5, map.sizeOfNonEmptyColumns("odd"));

        // test
        map.remove(1, "odd");
        assertEquals(5, map.sizeOfNonEmptyColumns("even"));
        assertEquals(4, map.sizeOfNonEmptyColumns("odd"));
        assertEquals(0, map.sizeOfNonEmptyRows(1));
        assertFalse(map.getRow("odd").orElse(new ArrayList<>()).contains(1));

        map.removeColumn(1);
        assertEquals(5, map.sizeOfNonEmptyColumns("even"));
        assertEquals(4, map.sizeOfNonEmptyColumns("odd"));
        assertFalse(map.getColumn(1).isPresent());

        map.removeRow("even");
        assertFalse(map.getRow("even").isPresent());
    }

    @Test
    void testUniqueness() {
        // precondition
        assertFalse(otherMap.getColumn("even").isPresent());

        // test
        otherMap.add("even", true);
        otherMap.add("even", true);
        final Optional<List<Boolean>> columnOpt = otherMap.getColumn("even");

        assertTrue(columnOpt.isPresent());
        assertEquals(1, columnOpt.get().size());
    }
}
