package de.associations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.associations.mock.Foo;
import de.associations.mock.Moo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AssociationBehaviourTest {

    private Map<Foo, List<Moo>> associations;
    private AssociationBehaviour<Foo, Moo> ab1, ab2, ab3;
    private AssociationBehaviour<Moo, Foo> ab4;
    private Foo f1;
    private Moo m1, m2;

    @BeforeEach
    void setup() {
		associations = new HashMap<>();
		ab1 = new AssociationBehaviour<>(Foo.class, Moo.class,
				foo -> associations.get(foo).size());
		ab2 = new AssociationBehaviour<>(Foo.class, Moo.class,
				foo -> associations.get(foo).size());
		ab3 = new AssociationBehaviour<>(Foo.class, Moo.class,
				foo -> associations.get(foo).size());
		ab4 = new AssociationBehaviour<>(Moo.class, Foo.class, moo -> 0);
	
		f1 = new Foo("f1", 1);
		m1 = new Moo("m1", 1);
		m2 = new Moo("m2", 1);
    }
    
    @AfterEach
    void tearDown() {
		Foo.resetIds();
		Moo.resetIds();
    }
    
    @Test
    void testZeroToOneBorder() {
    	// setup
		ab1.setMinimumToZero();
		ab1.setMaximumToOne();
		associations.put(f1, new ArrayList<>());

		// precondition
		assertTrue(ab1.appliesTo(f1));
		assertFalse(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));
	
		associations.get(f1).add(m1);
		assertTrue(ab1.appliesTo(f1));
		assertTrue(ab1.canRemove(f1));
		assertFalse(ab1.canAppend(f1));
    }
    
    @Test
    void testZeroToUnlimitedBorder() {
    	// setup
		ab1.setBoundaries("0..*");
		associations.put(f1, new ArrayList<>());

		// precondition
		assertFalse(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));

		// test
		for(int i = 0; i < Short.MAX_VALUE; ++i) {
	    	associations.get(f1).add(new Moo("moo_" + i, 1));
	    	assertTrue(ab1.canRemove(f1));
	    	assertTrue(ab1.canAppend(f1));
		}
    }
    
    @Test
    void testZeroToDynamicBorder() {
    	// setup
		ab1.setBoundaries("0..limitMoo");
		associations.put(f1, new ArrayList<>());

		// precondition
		assertFalse(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));

		// test
		associations.get(f1).add(m1);
		assertTrue(ab1.canRemove(f1));
		assertFalse(ab1.canAppend(f1));
	
		f1.setLimit(2);
		assertTrue(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));
	
		associations.get(f1).add(m2);
		assertTrue(ab1.canRemove(f1));
		assertFalse(ab1.canAppend(f1));
    }
    
    @Test
    void testDynamicToUnlimitedBorderInjected() {
		ab1.setMinimum("limitMoo", Foo::limitMoo);
		checkDynamicMinimum();
    }
    
    private void checkDynamicMinimum() {
    	// setup
		f1.setLimit(1);
		associations.clear();
		associations.put(f1, new ArrayList<>());
		associations.get(f1).add(m1);

		// precondition
		assertFalse(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));

		// test
		f1.setLimit(0);
		assertTrue(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));
    }

	@Test
	void testDynamicToUnlimitedBorderAutomaticDetection() {
		ab1.setBoundaries("limitMoo..*");
		checkDynamicMinimum();
	}
    
    @Test
    void testOneToOneBorder() {
    	// setup
		ab1.setMinimumToOne();
		ab1.setMaximumToOne();
		associations.put(f1, new ArrayList<>());

		// precondition
		assertFalse(ab1.canRemove(f1));
		assertTrue(ab1.canAppend(f1));

		// test
		associations.get(f1).add(m1);
		assertFalse(ab1.canRemove(f1));
		assertFalse(ab1.canAppend(f1));
    }
    
    @Test
    void testToStringWithStringBoundaries() {
    	// setup
		ab1.setBoundaries("-1..-2");

		// test
		assertEquals("Foo --> [0..*] Moo", ab1.toString());
    }

    @Test
	void testToStringWithName() {
    	// setup
		ab1.setBoundaries("0..limitMoo");

		// test
		assertEquals("Foo --> [0..limitMoo] Moo", ab1.toString());
	}

    @Test
	void testToStringWithNumbers() {
    	// setup
		ab1.setMinimum(2);
		ab1.setMaximum(5);

		// test
		assertEquals("Foo --> [2..5] Moo", ab1.toString());
	}
    
    @Test
    void testInvalidBorderName() {
		// setup
		final Runnable exec = () -> ab1.setBoundaries("x..*");
		final String msg = "Can't access method 'x' of class '" +
				f1.getClass().getCanonicalName() + "'";

		// test
		assertRuntimeThrows(exec, msg);
    }
    
    @Test
    void testInvalidBorderSyntax() {
    	// setup
		final Runnable exec = () -> ab1.setBoundaries("1..2..3");
		final String msg = "Expected expression of form '(num|func)..(num|func|*)'";

		// test
		assertRuntimeThrows(exec, msg);
    }
    
    private void assertRuntimeThrows(Runnable r, String msg) {
		try {
	    	r.run();
	    	fail();
		} catch(RuntimeException e) {
	   		assertEquals(msg, e.getMessage());
		}
    }
    
    @Test
    void testEquals() {
    	// setup
		ab1.setBoundaries("0..1");
		ab2.setBoundaries("0..1");
		ab3.setBoundaries("0..*");
		ab4.setBoundaries("0..1");

		// test
		assertEquals(ab1, ab1);
		assertNotEquals(null, ab1);
		assertNotEquals("potatoe", ab1);
		assertEquals(ab1, ab2);
		assertNotEquals(ab1, ab3);
		assertNotEquals(ab1, ab4);
    }
    
    @Test
    void testHashCode() {
    	// setup
		ab1.setBoundaries("0..1");
		ab2.setBoundaries("0..1");
		ab3.setBoundaries("0..*");
		ab4.setBoundaries("0..1");

		// test
		assertEquals(ab1.hashCode(), ab2.hashCode());
		assertNotEquals(ab1.hashCode(), ab3.hashCode());
		assertNotEquals(ab1.hashCode(), ab4.hashCode());
    }
    
    @Test
    void testNegativeBordersWithStrings() {
    	// setup
		ab1.setBoundaries("-1..-1");
		ab2.setBoundaries("0..*");

		// test
		assertEquals(ab2, ab1);
    }

	@Test
	void testNegativeBordersWithIntegers() {
    	// setup
		ab2.setBoundaries("0..*");
		ab1.setMinimum(-1);
		ab1.setMaximum(-1);

		// test
		assertEquals(ab2, ab1);
	}

	@Test
	void testMaximumWithIllegalAccessExceptionException() {
		// setup
		ab1.setBoundaries("0..limitMooThrowIllegalAccessException");

		// test
		assertThrows(AssociationException.class, () -> ab1.checkUpperBorder(f1, 0));
	}

	@Test
	void testMaximumWithIllegalArgumentException() {
		// setup
		ab1.setBoundaries("0..limitMooThrowIllegalArgumentException");

		// test
		assertThrows(AssociationException.class, () -> ab1.checkUpperBorder(f1, 0));
	}

	@Test
	void testMaximumWithThrowInvocationTargetException() {
		// setup
		ab1.setBoundaries("0..limitMooThrowInvocationTargetException");

		// test
		assertThrows(AssociationException.class, () -> ab1.checkUpperBorder(f1, 0));
	}
}
