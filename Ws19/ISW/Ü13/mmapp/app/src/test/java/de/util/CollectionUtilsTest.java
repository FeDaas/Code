package de.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.util.CollectionUtils.split;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionUtilsTest {
    @Test
    void testSplit() {
        // setup
        final List<Integer> list = asList(1, 2, 3, 0, 4, 5, 0, 6, 7, 8, 9);
        final List<List<Integer>> expected = asList(
                asList(1, 2, 3),
                asList(4, 5),
                asList(6, 7, 8, 9)
        );
        int border = 0;

        // precondition
        assertTrue(list.contains(border));

        // test
        assertEquals(expected, split(list, border));
    }

    @Test
    void testSplitWithNonExistingBorder() {
        // setup
        final List<Integer> list = asList(1, 2, 3, 0, 4, 5, 0, 6, 7, 8, 9);
        final List<List<Integer>> expected = singletonList(
                asList(1, 2, 3, 0, 4, 5, 0, 6, 7, 8, 9)
        );
        int border = -1;

        // precondition
        assertFalse(list.contains(border));

        // test
        assertEquals(expected, split(list, border));
    }

    @Test
    void testSplitWithEmptyList() {
        // setup
        final List<Integer> list = emptyList();
        final List<List<Integer>> expected = emptyList();

        // test
        assertEquals(expected, split(list, 0));
    }
}
