package de.util;

import org.junit.jupiter.api.Test;

import static de.util.PrimitiveUtils.fromObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitiveUtilsTest {
    @Test
    void testFromObjectWithNonNullBoolean() {
        assertFalse(fromObject(Boolean.FALSE));
        assertTrue(fromObject(Boolean.TRUE));
    }

    @Test
    void testFromObjectWithNullBoolean() {
        assertFalse(fromObject(ObjectUtils.<Boolean>typeSafeNull()));
    }

    @Test
    void testFromObjectWithNonNullInteger() {
        assertEquals(-256, fromObject(-256));
        assertEquals(0, fromObject(0));
        assertEquals(256, fromObject(256));
    }

    @Test
    void testFromObjectWithNullInteger() {
        assertEquals(0, fromObject(ObjectUtils.<Integer>typeSafeNull()));
    }
}
