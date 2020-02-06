package de.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static de.util.Pair.paired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PairTest {
    private int a;
    private double b;
    private Map<Integer, Double> m;
    
    @BeforeEach
	void init() {
		this.a = 42;
		this.b = 6.9;
	
		m = new HashMap<>();
		m.put(a, b);
    }

    @Test
    void testPublicAccessAndGetters() {
    	// setup
		final Pair<Integer, Double> p1 = new Pair<>(a, b);

		// test
		assertEquals(a, p1.first.intValue());
		assertEquals(b, p1.second.doubleValue());
		assertEquals(p1.first, p1.getFirst());
		assertEquals(p1.second, p1.getSecond());
    }

    @Test
    void testEqualsAndHashCode() {
    	// setup
		final Pair<Integer, Double> p1 = new Pair<>(a, b);
		final Pair<Integer, Double> p2 = paired(a, b);
		final Pair<Integer, Double> p3 = paired(m.entrySet().iterator().next());

		// test
		assertNotEquals(null, p1);
		assertNotEquals("potatoe", p1);
		assertEquals(p1, p1);
		assertEquals(p1, p2);
		assertEquals(p1, p3);
		assertEquals(p1.hashCode(), p2.hashCode());
		assertEquals(p1.hashCode(), p3.hashCode());
    }
    
    @Test
    void testSwapArguments() {
    	// setup
		final Pair<Integer, Double> p1 = new Pair<>(a, b);
		final Pair<Double, Integer> p4 = p1.swapArgs();

		// test
		assertEquals(p1.first, p4.second);
		assertEquals(p1.second, p4.first);
    }
    
    @Test
    void testToString() {
    	// setup
		final Pair<Integer, Double> p1 = new Pair<>(a, b);
		final String expected = String.format("(%s, %s)", a, b);

		// test
		assertEquals(expected, p1.toString());
    }
    
    @Test
    void testNonNullRequirement() {
    	// setup
    	final Class<NullPointerException> excClass = NullPointerException.class;

    	// test
		assertThrows(excClass, () -> paired(null, b));
		assertThrows(excClass, () -> paired(a, null));
		assertThrows(excClass, () -> paired(null, null));
    }

    @Test
	void testMapper() {
    	// setup
    	final Pair<String, String> pair = new Pair<>("123", "456");

    	// test
    	assertEquals(paired(123, "456"), pair.mapFirst(Integer::parseInt));
		assertEquals(paired("123", 456), pair.mapSecond(Integer::parseInt));
	}
}
