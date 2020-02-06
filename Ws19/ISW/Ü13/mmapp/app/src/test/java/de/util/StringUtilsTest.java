package de.util;

import org.junit.jupiter.api.Test;

import static de.util.StringUtils.alphabeticalComparison;
import static de.util.StringUtils.capitalizeString;
import static de.util.StringUtils.editDistance;
import static de.util.StringUtils.join;
import static de.util.StringUtils.normedMinimumEditDistance;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {
    @Test
    void testMinimumEditDistance() {
        // test
        assertEquals(1, editDistance("Mouse", "House"));
        assertEquals(1, editDistance("House", "Houses"));
        assertEquals(3, editDistance("Mice", "Mouse"));

    }

    @Test
    void testMinMinimumEditDistance() {
        // test
        assertEquals(0, editDistance("", ""));
        assertEquals(0, editDistance("Hello", "Hello"));
    }


    @Test
    void testMaxMinimumEditDistance() {
        // test
        assertEquals(13, editDistance("", "Hello, World!"));
    }

    @Test
    void testMinNormedMinimumEditDistance() {
        // test
        assertEquals(0.0, normedMinimumEditDistance("", "Hello, World!"));
    }

    @Test
    void testMaxNormedMinimumEditDistance() {
        // test
        assertEquals(1.0, normedMinimumEditDistance("", ""));
        assertEquals(1.0, normedMinimumEditDistance("Hello", "Hello"));
    }

    @Test
    void testNormedMinimumEditDistanceWithDelta() {
        // test
        assertEquals(0.8, normedMinimumEditDistance("Mouse", "House"), 1e-4);
        assertEquals(0.83333, normedMinimumEditDistance("House", "Houses"), 1e-4);
        assertEquals(0.4, normedMinimumEditDistance("Mice", "Mouse"), 1e-4);
    }

    @Test
    void testCapitalizationWithEmptyString() {
        // test
        assertEquals("", capitalizeString(""));
    }

    @Test
    void testCapitalizationWithOneLetter() {
        // test
        assertEquals("A", capitalizeString("a"));
    }

    @Test
    void testCapitalizationWithOneNumber() {
        // test
        assertEquals("4", capitalizeString("4"));
    }

    @Test
    void testCapitalizationWithMixedCaseLettersOnly() {
        // test
        assertEquals("Hello", capitalizeString("hello"));
        assertEquals("Hello", capitalizeString("HELLO"));
        assertEquals("Hello", capitalizeString("HeLlO"));
    }

    @Test
    void testCapitalizationWithNumbersAndLetters() {
        // test
        assertEquals("Hello123", capitalizeString("hello123"));
    }

    @Test
    void testJoinWithNoArgs() {
        assertEquals("", join("---"));
    }

    @Test
    void testAlphabeticalComparisonWithIdenticalString() {
        // setup
        String s1 = "abc";

        // test
        assertEquals(0, alphabeticalComparison(s1, s1));
    }

    @Test
    void testAlphabeticalComparisonWithOneCharDifference() {
        // setup
        String s1 = "abc";
        String s2 = "abd";

        // test
        assertEquals(-1, alphabeticalComparison(s1, s2));
        assertEquals(1, alphabeticalComparison(s2, s1));
    }

    @Test
    void testAlphabeticalComparisonWithLongerString() {
        // setup
        String s1 = "abc";
        String s2 = "abcdefg";

        // test
        assertEquals(4, alphabeticalComparison(s2, s1));
        assertEquals(-4, alphabeticalComparison(s1, s2));
    }
}
