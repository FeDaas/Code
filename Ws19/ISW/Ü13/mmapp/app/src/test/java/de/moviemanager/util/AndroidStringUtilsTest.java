package de.moviemanager.util;

import android.os.Build;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static de.moviemanager.util.AndroidStringUtils.IDENTIFIER_LENGTH;
import static de.moviemanager.util.AndroidStringUtils.buildQueryPredicate;
import static de.moviemanager.util.AndroidStringUtils.join;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Config(sdk = {Build.VERSION_CODES.N_MR1, Build.VERSION_CODES.O})
public class AndroidStringUtilsTest {

    @Test
    public void testJoinWithEmptyArgs() {
        assertEquals("", join(","));
        assertEquals("", join(",", new ArrayList<>()));
        assertEquals("", join(",", Object::toString, new ArrayList<>()));
    }

    @Test
    public void testJoinWithArgs() {
        // setup
        final String expected = "1,3.4,true,null";
        final List<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("3.4");
        strings.add("true");
        strings.add("null");

        // test
        assertEquals(expected, join(",", "1", "3.4", "true", "null"));
        assertEquals(expected, join(",", strings));
        assertEquals(expected, join(",", obj -> "" + obj, asList(1, 3.4, true, null)));
    }

    @Test
    public void testGenerateIdentifier() {
        // setup
        final AtomicInteger integer = new AtomicInteger(10);
        final Predicate<String> condition = str -> integer.decrementAndGet() > 0;

        // test
        final String key = AndroidStringUtils.generateIdentifier(condition);
        assertNotNull(key);
        assertEquals(IDENTIFIER_LENGTH, key.length());
    }

    @Test
    public void testBuildQueryPredicate() {
        // setup
        final BiPredicate<Boolean, String> predicate = buildQueryPredicate(
                String::contains,
                b -> b + "",
                String::toLowerCase
        );

        // test
        assertTrue(predicate.test(true, "tRu"));
        assertTrue(predicate.test(true, "T"));
        assertTrue(predicate.test(true, "e"));
        assertFalse(predicate.test(true, "5"));

        assertTrue(predicate.test(false, "fAl"));
        assertTrue(predicate.test(false, "F"));
        assertTrue(predicate.test(false, "e"));
        assertFalse(predicate.test(false, "5"));
    }
}
